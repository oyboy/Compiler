bits 64
section .data
L_str_1 db "Sorted array:", 0
L_str_0 db "Initial array:", 0

section .text
global main
extern exit, printf, malloc, free, scanf, puts, getchar, pow, sqrt, sin, cos, strlen
extern print_int, print_bool, print_string, print_float

_start:
    call main
    mov rdi, rax
    call exit

partition:
    push rbp
    mov rbp, rsp
    sub rsp, 288
    mov qword [rbp - 8], rdi
    mov qword [rbp - 16], rsi
    mov qword [rbp - 24], rdx
.partition_entry:
    mov r10, [rbp - 8]
    mov r11, [rbp - 24]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 72], rax
    mov rax, [rbp - 72]
    mov [rbp - 32], rax
    mov rax, [rbp - 16]
    mov r10, 1
    sub rax, r10
    mov [rbp - 80], rax
    mov rax, [rbp - 80]
    mov [rbp - 40], rax
    mov rax, [rbp - 16]
    mov [rbp - 48], rax
    jmp .partition_L_for
.partition_L_for:
    mov rax, [rbp - 48]
    mov [rbp - 88], rax
    mov rax, [rbp - 88]
    cmp rax, [rbp - 24]
    setl al
    movzx rax, al
    mov [rbp - 96], rax
    mov rax, [rbp - 96]
    test rax, rax
    jnz .partition_L_forbody
    jmp .partition_L_endfor
.partition_L_forbody:
    mov rax, [rbp - 48]
    mov [rbp - 104], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 104]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 112], rax
    mov rax, [rbp - 32]
    mov [rbp - 120], rax
    mov rax, [rbp - 112]
    cmp rax, [rbp - 120]
    setl al
    movzx rax, al
    mov [rbp - 128], rax
    mov rax, [rbp - 128]
    test rax, rax
    jnz .partition_L_true
    jmp .partition_L_endif
.partition_L_true:
    mov rax, [rbp - 40]
    mov [rbp - 136], rax
    mov rax, [rbp - 136]
    mov r10, 1
    add rax, r10
    mov [rbp - 144], rax
    mov rax, [rbp - 144]
    mov [rbp - 40], rax
    mov rax, [rbp - 40]
    mov [rbp - 152], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 152]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 160], rax
    mov rax, [rbp - 160]
    mov [rbp - 56], rax
    mov rax, [rbp - 48]
    mov [rbp - 168], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 168]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 176], rax
    mov rax, [rbp - 40]
    mov [rbp - 184], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 184]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 176]
    mov [r10], rax
    mov rax, [rbp - 56]
    mov [rbp - 192], rax
    mov rax, [rbp - 48]
    mov [rbp - 200], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 200]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 192]
    mov [r10], rax
    jmp .partition_L_endif
.partition_L_endif:
    mov rax, [rbp - 48]
    mov [rbp - 208], rax
    mov rax, [rbp - 208]
    mov r10, 1
    add rax, r10
    mov [rbp - 216], rax
    mov rax, [rbp - 216]
    mov [rbp - 48], rax
    jmp .partition_L_for
.partition_L_endfor:
    mov rax, [rbp - 40]
    mov [rbp - 224], rax
    mov rax, [rbp - 224]
    mov r10, 1
    add rax, r10
    mov [rbp - 232], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 232]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 240], rax
    mov rax, [rbp - 240]
    mov [rbp - 64], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 24]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 248], rax
    mov rax, [rbp - 40]
    mov [rbp - 256], rax
    mov rax, [rbp - 256]
    mov r10, 1
    add rax, r10
    mov [rbp - 264], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 264]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 248]
    mov [r10], rax
    mov rax, [rbp - 64]
    mov [rbp - 272], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 24]
    shl r11, 3
    add r10, r11
    mov rax, [rbp - 272]
    mov [r10], rax
    mov rax, [rbp - 40]
    mov [rbp - 280], rax
    mov rax, [rbp - 280]
    mov r10, 1
    add rax, r10
    mov [rbp - 288], rax
    mov rax, [rbp - 288]
    leave
    ret

quicksort:
    push rbp
    mov rbp, rsp
    sub rsp, 80
    mov qword [rbp - 8], rdi
    mov qword [rbp - 16], rsi
    mov qword [rbp - 24], rdx
.quicksort_entry:
    mov rax, [rbp - 16]
    cmp rax, [rbp - 24]
    setl al
    movzx rax, al
    mov [rbp - 40], rax
    mov rax, [rbp - 40]
    test rax, rax
    jnz .quicksort_L_true
    jmp .quicksort_L_endif
.quicksort_L_true:
    mov rdi, [rbp - 8]
    mov rsi, [rbp - 16]
    mov rdx, [rbp - 24]
    call partition
    mov [rbp - 48], rax
    mov rax, [rbp - 48]
    mov [rbp - 32], rax
    mov rax, [rbp - 32]
    mov [rbp - 56], rax
    mov rax, [rbp - 56]
    mov r10, 1
    sub rax, r10
    mov [rbp - 64], rax
    mov rdi, [rbp - 8]
    mov rsi, [rbp - 16]
    mov rdx, [rbp - 64]
    call quicksort
    mov rax, [rbp - 32]
    mov [rbp - 72], rax
    mov rax, [rbp - 72]
    mov r10, 1
    add rax, r10
    mov [rbp - 80], rax
    mov rdi, [rbp - 8]
    mov rsi, [rbp - 80]
    mov rdx, [rbp - 24]
    call quicksort
    jmp .quicksort_L_endif
.quicksort_L_endif:
    leave
    ret

main:
    push rbp
    mov rbp, rsp
    sub rsp, 144
.main_entry:
    mov rdi, 48
    xor rax, rax
    call malloc
    mov [rbp - 32], rax
    mov rax, [rbp - 32]
    mov [rbp - 8], rax
    mov r10, [rbp - 8]
    mov r11, 0
    shl r11, 3
    add r10, r11
    mov rax, 10
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 1
    shl r11, 3
    add r10, r11
    mov rax, 7
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 2
    shl r11, 3
    add r10, r11
    mov rax, 8
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 3
    shl r11, 3
    add r10, r11
    mov rax, 9
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 4
    shl r11, 3
    add r10, r11
    mov rax, 1
    mov [r10], rax
    mov r10, [rbp - 8]
    mov r11, 5
    shl r11, 3
    add r10, r11
    mov rax, 5
    mov [r10], rax
    lea rdi, [L_str_0]
    call print_string
    mov qword [rbp - 16], 0
    jmp .main_L_for
.main_L_for:
    mov rax, [rbp - 16]
    mov [rbp - 40], rax
    mov rax, [rbp - 40]
    cmp rax, 6
    setl al
    movzx rax, al
    mov [rbp - 48], rax
    mov rax, [rbp - 48]
    test rax, rax
    jnz .main_L_forbody
    jmp .main_L_endfor
.main_L_forbody:
    mov rax, [rbp - 16]
    mov [rbp - 56], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 56]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 64], rax
    mov rdi, [rbp - 64]
    call print_int
    mov rax, [rbp - 16]
    mov [rbp - 72], rax
    mov rax, [rbp - 72]
    mov r10, 1
    add rax, r10
    mov [rbp - 80], rax
    mov rax, [rbp - 80]
    mov [rbp - 16], rax
    jmp .main_L_for
.main_L_endfor:
    mov rax, [rbp - 8]
    mov [rbp - 88], rax
    mov rdi, [rbp - 88]
    mov rsi, 0
    mov rdx, 5
    call quicksort
    lea rdi, [L_str_1]
    call print_string
    mov qword [rbp - 24], 0
    jmp .main_L_for_0
.main_L_for_0:
    mov rax, [rbp - 24]
    mov [rbp - 96], rax
    mov rax, [rbp - 96]
    cmp rax, 6
    setl al
    movzx rax, al
    mov [rbp - 104], rax
    mov rax, [rbp - 104]
    test rax, rax
    jnz .main_L_forbody_1
    jmp .main_L_endfor_3
.main_L_forbody_1:
    mov rax, [rbp - 24]
    mov [rbp - 112], rax
    mov r10, [rbp - 8]
    mov r11, [rbp - 112]
    shl r11, 3
    add r10, r11
    mov rax, [r10]
    mov [rbp - 120], rax
    mov rdi, [rbp - 120]
    call print_int
    mov rax, [rbp - 24]
    mov [rbp - 128], rax
    mov rax, [rbp - 128]
    mov r10, 1
    add rax, r10
    mov [rbp - 136], rax
    mov rax, [rbp - 136]
    mov [rbp - 24], rax
    jmp .main_L_for_0
.main_L_endfor_3:
    mov rax, [rbp - 8]
    mov [rbp - 144], rax
    mov rdi, [rbp - 144]
    call free
    mov rax, 0
    leave
    ret

