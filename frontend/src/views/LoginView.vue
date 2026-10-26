<template>
    <main class="grid min-h-screen bg-linear-to-br/hsl from-sky-900 from-30% via-slate-800 to-blue-900 place-items-center shadow-2xl">
        <div class="bg-white/10 backdrop-blur-md border border-white/20 shadow-xl p-10 rounded-xl text-center max-w-sm w-full">
            <header>
                <img src="/cti-logo.png" alt="CTI" class="h-20 block mx-auto">
                <h1 class="font-bold text-4xl text-white">Login</h1>
                <p class="text-md text-white mb-2">Faça login na sua conta</p>
            </header>
            <form class="flex flex-col gap-2">
                <div class="flex flex-col gap-2 text-left">
                    <label for="email" class="text-white" >E-mail </label>
                    <input id="email" type="email" placeholder="seu email" class="border-1 border-white rounded-xl text-center w-full text-white">
                </div>
                <div class="flex flex-col gap-2 text-left">
                    <label for="senha" class="text-white">Senha </label>
                    <input id="senha" type="password" placeholder="sua senha" class="border-1 border-white rounded-xl text-center w-full text-white">
                </div>
                <p></p>
                <button class="bg-blue-700 rounded-xl text-center px-3 py-1 hover:bg-white hover:border-sky-700 hover:text-sky-700 transition duration-300 cursor-pointer">Login</button>
            </form>
            <footer class="text-sm">
                <p class="text-white">Não tem uma conta?<RouterLink to="/cadastro" class="text-blue-600"> Cadastre-se!</RouterLink></p>
            </footer>
        </div>
    </main>
</template>