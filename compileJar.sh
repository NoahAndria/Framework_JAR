rm -r classes
mkdir -p classes

javac -cp compilelib/servlet-api.jar -d classes framework/src/*
jar cf my-framework.jar -C classes .