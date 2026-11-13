<script setup>
    import { ref } from 'vue'
    import { useAuthStore } from '@/stores/auth'
    import { mensagemDeErro } from '@/services/api'

    const auth = useAuthStore()

    const nome = ref('')
    const email = ref('')
    const senha = ref('')
    const repetida = ref('')
    const erro = ref('')
    const enviado = ref(false)
    const carregando = ref(false)

    async function cadastrar() {
        erro.value = ''
        if (senha.value.length < 6) return (erro.value = 'A senha precisa ter pelo menos 6 caracteres.')
        if (senha.value !== repetida.value) return (erro.value = 'As senhas não são iguais.')
        carregando.value = true
        try {
            await auth.cadastrar(nome.value, email.value, senha.value)
            enviado.value = true
        } catch (e) {
            erro.value = mensagemDeErro(e)
        } finally {
            carregando.value = false
        }
    }
</script>
<template>
    <main class="grid min-h-screen bg-linear-to-br/hsl from-sky-900 from-30% via-slate-800 to-blue-900 place-items-center shadow-2xl">
        <div class="bg-white/10 backdrop-blur-md border border-white/20 shadow-xl p-10 rounded-xl text-center max-w-sm w-full">
            <header>
                <RouterLink to="/"><img src="/cti-logo.png" alt="CTI" class="h-20 block mx-auto"></RouterLink>
                <h1 class="font-bold text-4xl text-white">Cadastro</h1>
                <p class="text-md text-white mb-2">Solicite acesso ao sistema</p>
            </header>

            <div v-if="enviado" class="text-white flex flex-col gap-4">
                <p>Pedido enviado! Um administrador precisa aprovar seu acesso antes do primeiro login.</p>
                <RouterLink to="/login" class="bg-blue-700 rounded-xl px-3 py-1 hover:bg-white hover:text-sky-700 transition duration-300">Ir para o login</RouterLink>
            </div>

            <form v-else @submit.prevent="cadastrar" class="flex flex-col gap-2">
                <div class="flex flex-col gap-1 text-left">
                    <label for="email" class="text-white">E-mail </label>
                    <input id="email" v-model="email" type="email" required autocomplete="email" placeholder="seu email" class="border-1 border-white rounded-xl text-center w-full text-white">
                </div>
                <div class="flex flex-col gap-1 text-left">
                    <label for="nome" class="text-white">Nome </label>
                    <input id="nome" v-model="nome" type="text" required maxlength="120" autocomplete="name" placeholder="seu nome" class="border-1 border-white rounded-xl text-center w-full text-white">
                </div>
                <div class="flex flex-col gap-1 text-left">
                    <label for="senha" class="text-white">Senha </label>
                    <input id="senha" v-model="senha" type="password" required minlength="6" autocomplete="new-password" placeholder="senha" class="border-1 border-white rounded-xl text-center w-full text-white">
                </div>
                <div class="flex flex-col gap-1 text-left">
                    <label for="Rsenha" class="text-white">Repita a senha </label>
                    <input id="Rsenha" v-model="repetida" type="password" required autocomplete="new-password" placeholder="senha" class="border-1 border-white rounded-xl text-center w-full text-white">
                </div>
                <p v-if="erro" class="text-red-300 text-sm" role="alert">{{ erro }}</p>
                <button type="submit" :disabled="carregando" class="bg-blue-700 text-white rounded-xl text-center px-3 py-1 hover:bg-white hover:border-sky-700 hover:text-sky-700 transition duration-300 cursor-pointer disabled:opacity-50 mt-2">
                    {{ carregando ? 'Enviando...' : 'Cadastrar' }}
                </button>
            </form>
            <footer class="text-sm mt-3">
                <p class="text-white">Já tem uma conta?<RouterLink to="/login" class="text-sky-300"> Entre!</RouterLink></p>
            </footer>
        </div>
    </main>
</template>
