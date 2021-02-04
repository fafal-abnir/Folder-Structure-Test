import os

# Press the green button in the gutter to run the script.import os
test_directory = "/data/test-fohi"
if __name__ == "__main__":
	os.system("java -jar fohi-all.jar -p {} -d 1 -b 1 -s 10000 -c 90000000".format(test_directory))
	print("Deleting files and directories")
	os.system("rm -r {}".format(test_directory))
	os.system("java -jar fohi-all.jar -p {} -d 1 -b 16 -s 10000 -c 90000000".format(test_directory))
	print("Deleting files and directories")
	os.system("rm -r {}".format(test_directory))
	os.system("java -jar fohi-all.jar -p {} -d 2 -b 16 -s 10000 -c 90000000".format(test_directory))
	print("Deleting files and directories")
	os.system("rm -r {}".format(test_directory))
	os.system("java -jar fohi-all.jar -p {} -d 3 -b 16 -s 10000 -c 90000000".format(test_directory))
	print("Deleting files and directories")
	os.system("rm -r {}".format(test_directory))
	os.system("java -jar fohi-all.jar -p {} -d 4 -b 16 -s 10000 -c 90000000".format(test_directory))
	print("Deleting files and directories")
	os.system("rm -r {}".format(test_directory))



