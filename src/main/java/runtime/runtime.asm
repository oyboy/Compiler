; runtime.asm - Linux x86-64
section .text
global exit_program, print_int

exit_program:
    mov rax, 60
    syscall

print_int:
    push rbp
    mov rbp, rsp
    sub rsp, 32
    mov rax, rdi
    mov rcx, 10
    lea rsi, [rbp-1]
    mov byte [rsi], 10
.loop:
    xor rdx, rdx
    div rcx
    add dl, '0'
    dec rsi
    mov [rsi], dl
    test rax, rax
    jnz .loop
    mov rax, 1
    mov rdi, 1
    mov rdx, rbp
    sub rdx, rsi
    syscall
    mov rsp, rbp
    pop rbp
    ret