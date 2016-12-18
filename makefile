
compile:
	javac CocoJNI.java
	javah CocoJNI
	gcc -I /usr/lib/jvm/java-8-oracle/include/ -I /usr/lib/jvm/java-8-oracle/include//linux -c CocoJNI.c 
	gcc -I /usr/lib/jvm/java-8-oracle/include/ -I /usr/lib/jvm/java-8-oracle/include//linux -o libCocoJNI.so -fPIC -shared CocoJNI.c
	javac Problem.java
	javac Benchmark.java
	javac Observer.java
	javac Suite.java
	javac ExampleExperiment.java
	javac Experiment.java -cp ".:jmetal.jar" 
	javac CocoProblem.java -cp ".:jmetal.jar" 

run:
	java -cp ".:jmetal.jar" -Djava.library.path=. Experiment -alg NSGAII -budget 100

init:
	git clone https://github.com/numbbo/coco.git
	python coco/do.py run-java
	python coco/do.py install-postprocessing
	cp -a coco/code-experiments/build/java/. ./

jmetaljar:
	export JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8
	find jmetal/ -name "*.java" > sources.aux
	mkdir -p build
	javac -d ./build @sources.aux
	jar cvf jmetal.jar build/*
	rm sources.aux
	

