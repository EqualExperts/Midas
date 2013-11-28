PORT_PREFIX=$1

mongoData=~/mongodata
mongoBase=$mongoData/$PORT_PREFIX
basePathSt=$mongoBase/standaloneData

rm -rf $mongoBase/

mkdir $mongoData/ $mongoBase/ $basePathSt/
mkdir $basePathSt/17

$MONGO_VERSIONS/$PORT_PREFIX/bin/mongod --port $PORT_PREFIX""17 --dbpath $basePathSt/17 --smallfiles

sleep 5
echo Mongo Started at $PORT_PREFIX""17