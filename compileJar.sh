rm -r classes
mkdir -p classes

javac -cp compilelib/servlet-api.jar -d classes framework/src/*

cd classes || exit

jar cvf my-framework.jar .
