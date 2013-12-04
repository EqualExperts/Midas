import sys
import os

print("Platform = " + sys.platform)
platform = sys.platform
if (platform == 'win32'):
    os.system("easy_install pymongo")
    os.system("easy_install unittest-xml-reporting")
elif (platform == 'linux2'):
    os.system("sudo pip install pymongo==2.6.3")
    os.system("sudo pip install unittest-xml-reporting")
elif(platform == 'darwin'):
    os.system("sudo python -m easy_install pymongo")
    os.system("sudo python -m easy_install unittest-xml-reporting")
else:
    print("Please add your platform here as in the current scheme it is not applicable")