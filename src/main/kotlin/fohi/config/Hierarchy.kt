package fohi.config

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class Hierarchy : CliktCommand("") {
    private val rootPath: String by option("-p", "--path",
                                           help = "Path of location for test").prompt(
            "Enter path for test")
    private val depth: Int by option("-d", "--depth", help = "Depth of directories for test").int()
            .prompt("Enter depth of hierarchy").validate {
                require(depth > 0) {
                    "Depth should be greater than 0."
                }
            }
    private val branch: Int by option("-b", "--branch",
                                      help = "Branching size of directories").int()
            .prompt("Enter branch size").validate {
                require(ceil(log(branch.toDouble(), 16.0)) == floor(log(branch.toDouble(), 16.0))) {
                    "Branch should be in power of 16 (16,256,..) "
                }
            }
    private val fileSize: Int by option("-s", "--size", help = "File size for test").int()
            .prompt("Enter file size for this test").validate {
                require(fileSize > 1) {
                    "file size should be greater than 0."
                }
            }
    private val fileCount: Int by option("-c", "--file-count", help = "File count for test").int()
            .prompt("Enter file count").validate {
                require(fileCount > 0) {
                    "file count should be greater than 0."
                }
            }
    private val threadCount: Int by option("-t", "--thread-count", help = "Thread count for test").int()
            .prompt("Enter thread count").validate {
                require(threadCount > 0) {
                    "threadCount count should be greater than 0."
                }
            }
    private val sep = File.separator


    private fun putGetTest() {
        val executorsService = Executors.newFixedThreadPool(threadCount)
        val writeCSVFilePath = "d:$depth-b:$branch-s:$fileSize-c:$fileCount-write.csv"
        val readCSVFilePath = "d:$depth-b:$branch-s:$fileSize-c:$fileCount-read.csv"
        val writer = Files.newBufferedWriter(Paths.get(writeCSVFilePath))
        val writeCSVPrinter = CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Count", "Latencies", "Duration"))
        val reader = Files.newBufferedWriter(Paths.get(readCSVFilePath))
        val readCSVPrinter = CSVPrinter(reader, CSVFormat.DEFAULT
                .withHeader("Count", "Latencies", "Duration"))
        println("Creating directories initiated.")
        val rootDir = File(rootPath)
        if (!rootDir.exists()) rootDir.mkdir()
        val t = measureTimeMillis { createDirectoryBranches(depth, rootPath) }
        println("Time of creating directories: $t ms")
        var randomBytes = ByteArray(fileSize)
        val writeLatencies = ArrayList<Long>()
        val readLatencies = ArrayList<Long>()
        var writeTime: Long
        var readTime: Long
        val starTime = System.currentTimeMillis()
        var duration: Long
        var filePath: String
        val readWriteList=ArrayList<Callable<Pair<Char,Long>>>()
        for (i in 0..(fileCount/100)) {
            for (j in 0..99){
                readWriteList.add(Callable {
                    filePath = getFilePath(i*100+j)
                    writeTime = measureTimeMillis {
                        FileOutputStream(filePath).use {
                            it.write(randomBytes)
                        }
                    }
                    'w' to writeTime
                })
            }
            if(i>5){
                for (j in 1..20){
                    readWriteList.add(Callable {
                        val x = Random.nextInt(1, (i-3)*100)
                        filePath = getFilePath(x)
                        readTime = measureTimeMillis {
                            try {
                                FileInputStream(filePath).use {
                                    randomBytes = it.readBytes()
                                }
                            } catch (e:Throwable){
                                println(e.stackTrace)
                                println("$x $filePath")
                            }
                        }
                        'r' to readTime
                    })
                }
            }

            val results = executorsService.invokeAll(readWriteList)
            for (result in results){
                if(result.get().first=='w'){
                    writeLatencies.add(result.get().second)
                }
                else {
                    readLatencies.add(result.get().second)
                }
            }
            duration = System.currentTimeMillis() - starTime
            println("Count:${i*100}  Average_Write_Latency:${writeLatencies.average()} duration:$duration")
            writeCSVPrinter.printRecord(i, writeLatencies.average(), duration)
            writeCSVPrinter.flush()
            writeLatencies.clear()
            duration = System.currentTimeMillis() - starTime
            println("Count:${i*100}  Average_Read_Latency:${readLatencies.average()} duration:$duration")
            readCSVPrinter.printRecord(i, readLatencies.average(), duration)
            readCSVPrinter.flush()
            readLatencies.clear()
            readWriteList.clear()
            }

        readCSVPrinter.close()
        writeCSVPrinter.close()
        executorsService.shutdown()
    }


    fun getFilePath(number: Int): String {
        val subDirectoryLengthName = log(branch.toDouble(), 16.0).toInt()
        val fileName: String
        val filePath: String
        val x = number.toString().sha512().hexString()
        if (branch == 1) {
            fileName = x
            filePath = "$rootPath$sep$fileName"
        } else {
            val p = x.take(subDirectoryLengthName * depth)
                    .chunked(subDirectoryLengthName)
                    .joinToString(sep)
            fileName = x.drop(subDirectoryLengthName * depth)
            filePath = "$rootPath$sep$p$sep$fileName"
        }
        return filePath
    }

    private fun createDirectoryBranches(counter: Int, location: String) {
        val subDirectoryLengthName = log(branch.toDouble(), 16.0).toInt()
        if (branch == 1)
            return
        for (i in 0..branch) {
            val dir = File("$location$sep${"%0${subDirectoryLengthName}x".format(i)}${sep}")
            if (!dir.exists()) dir.mkdir()
            if (counter > 1)
                createDirectoryBranches(counter - 1, "$location$sep${
                    "%0${subDirectoryLengthName}x".format(i)
                }${sep}")
        }
    }

    override fun run() {
        putGetTest()
    }
}
