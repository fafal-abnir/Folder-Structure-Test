package fohi.config

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

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
    private val branch: Int by option("-b", "--branch", help = "Branching size of directories").int()
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
    private val sep = File.separator


    private fun putTest() {
        val csvFilePath = "d:$depth-b:$branch-s:$fileSize-c:$fileCount.csv"
        val writer = Files.newBufferedWriter(Paths.get(csvFilePath))
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Count", "Latencies","Duration"))
        val subDirectoryLengthName = log(branch.toDouble(), 16.0).toInt()
        println("Creating directories initiated.")
        val rootDir = File(rootPath)
        if (!rootDir.exists()) rootDir.mkdir()
        val t = measureTimeMillis { createDirectoryBranches(depth, rootPath) }
        println("Time of creating directories: $t ms")
        val randomBytes = ByteArray(fileSize)
        val latencies = ArrayList<Long>()
        var time : Long
        val starTime = System.currentTimeMillis()
        var duration = System.currentTimeMillis()
        for (i in 0..fileCount) {
            val x = i.toString().sha512().hexString()
            val p = x.take(subDirectoryLengthName * depth)
                    .chunked(subDirectoryLengthName)
                    .joinToString(sep)
            val fileName = x.drop(subDirectoryLengthName * depth)
            val filePath = "$rootPath$sep$p$sep$fileName"
            time = measureTimeMillis {
                FileOutputStream(filePath).use {
                    it.write(randomBytes);
                }
            }
            latencies.add(time)
            if(i%1000==0){
                duration = System.currentTimeMillis() - starTime
                println("Count:${i}  Average_Latency${latencies.average()} duration:$duration")
                csvPrinter.printRecord(i, latencies.average(), duration)
                csvPrinter.flush()
                latencies.clear()
            }
        }
        csvPrinter.close()
    }

    private fun createDirectoryBranches(counter: Int, location: String) {
        val subDirectoryLengthName = log(branch.toDouble(), 16.0).toInt()
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
        putTest()
    }
}
