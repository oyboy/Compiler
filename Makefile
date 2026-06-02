.PHONY: build clean demo test-all

build:
	@echo "Building mycc..."
	@chmod +x mycc
	@./mycc --version

demo:
	@./mycc src/main/resources/hello.src -o program -v
	@./program

test-all:
	@echo "Running demo test..."
	@./mycc src/main/resources/hello.src -o program
	@./program

clean:
	rm -rf target
	rm -f sources.txt
	rm -f out.asm output.o runtime.o program a.out