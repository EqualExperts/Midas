if [ -z "$MONGO_VERSIONS"]; then
 echo "Please set MONGO_VERSIONS in bashrc"
else
 sh startMongodWithPortPrefix.sh 248 &
 sh startMongodWithPortPrefix.sh 226 &
 sh startMongodWithPortPrefix.sh 209 &
fi