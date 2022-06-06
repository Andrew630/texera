cd amber
if [ ! -z $1 ] && [ ! -z $2 ]; 
then 
    sbt "runMain edu.uci.ics.texera.web.TexeraRunWorker --masterIp $1 --workerIp $2" 
else
    sbt "runMain edu.uci.ics.texera.web.TexeraRunWorker" 
fi
