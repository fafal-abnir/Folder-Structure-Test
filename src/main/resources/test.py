import os

if __name__ == "__main__":
	os.system("java -jar fohi.jar -p /tmp/test-fohi -d 1 -b 16 -s 1024 -c 3000")
	os.system("rm -r /tmp/test-fohi/")
	os.system("java -jar fohi.jar -p /tmp/test-fohi -d 2 -b 16 -s 1024 -c 3000")
	os.system("rm -r /tmp/test-fohi/")
	os.system("java -jar fohi.jar -p /tmp/test-fohi -d 3 -b 16 -s 1024 -c 3000")


