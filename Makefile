NAME=fleval-1.0

build:
	mvn package

tar: clean
	mkdir -p ${NAME}
	cp -a AUTHORS LICENSE README.md doc man ${NAME}
	cp -a examples emacs scripts src  ${NAME}
	cp -a Makefile pom.xml ${NAME}
	tar zcf ${NAME}.tar.gz --owner=0 --group=0 ${NAME}
	rm -rf ${NAME}

clean:
	mvn clean
