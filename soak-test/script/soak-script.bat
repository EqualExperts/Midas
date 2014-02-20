#1. starting version v1 of clientApp connected to mongo on port 27017
start "client-v1" groovy -cp libs\mongo-java-driver-2.11.3.jar; Client.groovy --version=v1 --host=localhost --port=27017

#2. starting midas on default host=localhost and default port=27020 in default EXPANSION mode.
start "midas" /D midas midas

timeout 20

#3. throw in deltas for version v2
start groovy DeltaGenerator.groovy --version=v2

timeout 5

#4. stop v1 client
taskkill /F /T /FI "WINDOWTITLE eq client-v1*"

#5. starting version v2 of clientApp connected to Midas on port 27020
start "client-v2" groovy -cp libs\mongo-java-driver-2.11.3.jar Client.groovy --version=v2 --host=localhost --port=27020

timeout 10

#6. monitor the db for expansions and contractions to be complete
start /WAIT groovy -cp midas\libs\*  MigrationMonitor.groovy --version=v2

#7. change Midas to CONTRACTION mode.
groovy SwitchMidasMode.groovy -f midas\deltas\app\app.midas