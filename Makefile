default: classes

classes:
	javac -cp src/ -d ./ src/bplustree.java

clean:
	rm -f *.class
	rm -rf dsimpl/
	rm -f output_file.txt
