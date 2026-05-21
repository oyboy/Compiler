nasm -f elf64 ../../../../out.asm -o output.o

nasm -f elf64 ../runtime/runtime.asm -o runtime.o

gcc -no-pie output.o runtime.o -o program -lm

echo "Running program..."
./program
echo "Exit code: $?"