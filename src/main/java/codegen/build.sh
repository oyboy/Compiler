nasm -f elf64 ../../../../out.asm -o output.o

nasm -f elf64 ../runtime/runtime.asm -o runtime.o

ld output.o runtime.o -o program

echo "Running program..."
./program
echo "Exit code: $?"