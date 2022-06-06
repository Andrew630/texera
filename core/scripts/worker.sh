cd amber
if [ ! -z $1 ] 
then 
    sbt "runMain edu.uci.ics.texera.web.TexeraRunWorker $1" 
else
    sbt "runMain edu.uci.ics.texera.web.TexeraRunWorker" 
fi
