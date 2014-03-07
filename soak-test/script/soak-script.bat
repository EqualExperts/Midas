echo "1. starting midas on default host=localhost and default port=27020 in default EXPANSION mode."
start "midas" /D midas midas

pause

echo "2. starting version v0 of clientApp connected to mongo on port 27017"
start "client-v0" groovy -cp libs\mongo-java-driver-2.11.3.jar; Client.groovy --version=v0 --host=localhost --port=27020

timeout 20000

echo "3. throw in deltas for version v1"
start groovy DeltaGenerator.groovy --version=v1

timeout 10

echo "4. stop v0 client"
taskkill /F /T /FI "WINDOWTITLE eq client-v0*"

echo "5. starting version v1 of clientApp connected to Midas on port 27020"
start "client-v1" groovy -cp libs\mongo-java-driver-2.11.3.jar Client.groovy --version=v1 --host=localhost --port=27020

timeout 10

echo "6. monitor the db for expansions and contractions to be complete"
start /WAIT groovy -cp midas\libs\*  MigrationMonitor.groovy --version=v1

echo "7. change Midas to CONTRACTION mode."
groovy ModeChanger.groovy -f midas\deltas\app\app.midas