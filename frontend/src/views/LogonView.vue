<template>
    <main class="grid min-h-screen bg-linear-to-br/hsl from-sky-900 from-30% via-slate-800 to-blue-900 place-items-center shadow-2xl">
        <div class="bg-white/10 backdrop-blur-md border border-white/20 shadow-xl p-10 rounded-xl text-center max-w-sm w-full">
            <header>
                <img src="/cti-logo.png" alt="CTI" class="h-20 block mx-auto"">
                <h1 class="font-bold text-4xl text-white">Cadastro</h1>
                <p class="text-md text-white mb-2">Solilcite acesso ao sistema</p>
            </header>
            <form class="flex flex-col gap-2">
                <div class="flex flex-col gap-1 text-left">
                    <label for="email" class="text-white" >E-mail: </label>
                    <input id="email" type="email" placeholder="seu@email.com" class="border-1 border-white rounded-xl text-center w-full">
                </div>
                <div class="flex flex-col gap-1 text-left">
                    <label for="nome" class="text-white">Nome: </label>
                    <input id="nome" type="str" placeholder="seu nome" class="border-1 border-white rounded-xl text-center w-full">
                </div>
                <div class="flex flex-col gap-1 text-left">
                    <label for="senha" class="text-white">Senha: </label>
                    <input id="senha" type="str" placeholder="senha" class="border-1 border-white rounded-xl text-center w-full">
                </div>
                <div class="flex flex-col gap-1 text-left">
                    <label for="Rsenha" class="text-white">Repita a senha: </label>
                    <input id="Rsenha" type="str" placeholder="senha" class="border-1 border-white rounded-xl text-center w-full">
                </div>
                <p></p>
                <button class="bg-blue-700 rounded-xl text-center px-3 py-1 hover:bg-white hover:border-sky-700 hover:text-sky-700 transition duration-300 cursor-pointer">Cadastrar</button>
            </form>
            <footer class="text-sm">
                <p class="text-white">Já tem uma conta?<RouterLink to="/Login" class="text-blue-600"> Entre!</RouterLink></p>
            </footer>
        </div>
    </main>
</template>