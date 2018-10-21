<?xml version="1.0" encoding="UTF-8"?>

<jnlp spec="1.6+" version="" codebase="">
    <information>
        <title>Dicomcleaner</title>
        <vendor>Dicomcleaner Team</vendor>
    </information>

    <security>
        <all-permissions/>
    </security>

    <resources>
        <java version="9+" href="http://java.sun.com/products/autodl/j2se"
              java-vm-args="--add-modules java.xml.bind --add-exports=java.base/sun.net.www.protocol.http=ALL-UNNAMED --add-exports=java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-exports=java.base/sun.net.www.protocol.file=ALL-UNNAMED --add-exports=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED --add-exports=java.base/sun.net.www.protocol.jar=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.desktop/javax.imageio.stream=ALL-UNNAMED --add-opens=java.desktop/javax.imageio=ALL-UNNAMED --add-opens=java.desktop/com.sun.awt=ALL-UNNAMED"
              initial-heap-size="${ihs}" max-heap-size="${mhs}"/>
        <java version="9+"
              java-vm-args="--add-modules java.xml.bind --add-exports=java.base/sun.net.www.protocol.http=ALL-UNNAMED --add-exports=java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-exports=java.base/sun.net.www.protocol.file=ALL-UNNAMED --add-exports=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED --add-exports=java.base/sun.net.www.protocol.jar=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.desktop/javax.imageio.stream=ALL-UNNAMED --add-opens=java.desktop/javax.imageio=ALL-UNNAMED --add-opens=java.desktop/com.sun.awt=ALL-UNNAMED"
              initial-heap-size="${ihs}" max-heap-size="${mhs}"/>
        <j2se version="1.8+" href="http://java.sun.com/products/autodl/j2se" initial-heap-size="${ihs}"
              max-heap-size="${mhs}"/>
        <j2se version="1.8+" initial-heap-size="${ihs}" max-heap-size="${mhs}"/>

        <jar href="${cdb}/dicomCleaner.jar" main="true"/>

        <!-- Avoiding Unnecessary Update Checks -->
        <property name="jnlp.versionEnabled" value="${jnlp_jar_version}"/>

    </resources>

    <application-desc main-class="org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader" />

</jnlp>