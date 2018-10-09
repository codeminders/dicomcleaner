path=.;.\lib\additional;%path%
java -Xmx768m -Xms768m -cp ".\pixelmed.jar;.\lib\additional\pixelmed_imageio.jar;.\lib\additional\hsqldb.jar;.\lib\additional\commons-compress-1.12.jar;.\lib\additional\vecmath1.2-1.14.jar;.\lib\additional\commons-codec-1.3.jar;.\lib\additional\jmdns.jar;.\lib\additional\jai_imageio.jar;.\lib\additional\clibwrapper_jiio.jar" com.pixelmed.display.DicomImageViewer
pause
