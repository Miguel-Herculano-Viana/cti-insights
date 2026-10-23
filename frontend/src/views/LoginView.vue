<template>
    <main class="grid min-h-screen bg-linear-to-br/hsl from-sky-900 from-30% via-slate-800 to-blue-900 place-items-center shadow-2xl">
        <div class="bg-white/10 backdrop-blur-md border border-white/20 shadow-xl p-10 rounded-xl text-center max-w-sm w-full">
            <header>
                <img src="/cti-logo.png" alt="CTI" class="h-20 block mx-auto"">
                <h1 class="font-bold text-4xl text-white">Login</h1>
                <p class="text-md text-white">Faça login na sua conta</p>
            </header>
            <form>
                <div>
                    <label for="email" class="text-white" >E-mail: </label>
                    <input id="email" type="email" placeholder="seu@email.com" class="border-1 border-white rounded-xl text-center">
                </div>
                <div>
                    <label for="senha" class="text-white">Senha: </label>
                    <input id="senha" type="str" placeholder="sua senha" class="border-1 border-white rounded-xl text-center">
                </div>
                <p></p>
                <button>Login</button>
            </form>
            <footer>
                <p class="text-white">Não tem uma conta?<RouterLink to="/Logon" class="text-blue-600"> Cadastre-se!</RouterLink></p>
            </footer>
        </div>
    </main>
</template>