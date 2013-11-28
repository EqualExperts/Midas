
if "%1"=="" (SET PORT_PREFIX=270) else (SET PORT_PREFIX=%1)

SET mongoData=C:\ReleaseMongods\
SET mongoBase=%mongoData%\%PORT_PREFIX%\
SET basePathSt=%mongoBase%\standaloneData

RD /S /Q %mongoBase%\

mkdir %mongoData%\ %mongoBase%\ %basePathSt%\
mkdir %basePathSt%\17

START "Unsecure@%PORT_PREFIX%17" %MONGO_VERSIONS%\%PORT_PREFIX%\bin\mongod --port %PORT_PREFIX%""17 --dbpath %basePathSt%\17 --smallfiles &

timeout 5
echo "Mongo Started at %PORT_PREFIX%17"