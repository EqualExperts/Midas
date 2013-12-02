import sys
import os

print("Platform = " + sys.platform)
platform = sys.platform
if platform == 'nt':
    os.system("easy_install pymongo")
elif (platform == 'linux2'):
    os.system("pip install pymongo==2.6.3")
elif(platform == 'darwin'):
    os.system("sudo python -m easy_install pymongo")
else:
    print("Please add your platform here as in the current scheme it is not applicable")
