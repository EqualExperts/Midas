IF "%MONGO_VERSIONS%"=="" (ECHO Please set MONGO_VERSIONS environment variable) else (
CALL startMongodWithPortPrefix.bat 248
CALL startMongodWithPortPrefix.bat 226
CALL startMongodWithPortPrefix.bat 209
)