rm -r classes
mkdir -p classes

javac -cp compilelib/servlet-api.jar -d classes framework/src/myframework/annotations/*.java framework/src/myframework/utils/*.java   framework/src/myframework/controllers/*.java

cd classes || exit

jar cvf my-framework.jar .

mv my-framework.jar ../
cd ../ || exit

rm /home/noah/S4/'Web Dynamique'/Framework_TEST/lib/my-framework.jar
cp my-framework.jar /home/noah/S4/'Web Dynamique'/Framework_TEST/lib/