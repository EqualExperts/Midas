import os

if os.name == 'nt':
    os.system("easy_install pymongo")
else :
    os.system("pip install pymongo==2.6.3")
