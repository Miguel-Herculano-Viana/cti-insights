<template>
    <div class="min-h-screen bg-gray-900 text-white">
        <header id="hero" class="grid grid-cols-3 p-6 bg-slate-800 items-center shadow-2xl">
            <p class="text-3xl font-bold">
                <span class="block">CTI</span>
                <span class="block">Insights</span>
            </p>
            <nav class="justify-self-center flex gap-4">
                <a href="#sobre" class="rounded-xl px-2 border-2 border-transparent hover:border-slate-700 transition duration-300">Sobre</a>
                <a href="#recursos" class="rounded-xl px-2 border-2 border-transparent hover:border-slate-700 transition duration-300">Recursos</a>
                <a href="#contato" class="rounded-xl px-2 border-2 border-transparent hover:border-slate-700 transition duration-300">Contato</a>
            </nav>
            <RouterLink to="/Login" class="bg-sky-700 px-3 py-1 justify-self-end rounded-full border-2 border-transparent
            hover:bg-white 
            hover:border-sky-700
            hover:text-sky-700 transition duration-300">Login</RouterLink>
        </header>
        <main>
            <section class="grid grid-cols-2 min-h-[70vh] p-30 gap-0 bg-linear-to-br from-sky-900 via-gray-900 to-blue-900 items-center">
                <h1 class="font-bold text-5xl text-white justify-self-center text-right">Transforme<br>Dados<br>em<br>Decisões</h1>
                <div class="bg-white/10 backdrop-blur-md border border-white/20 shadow-xl p-10 rounded-xl ">
                    <img src="/cti-logo.png" alt="CTI" class="h-30 block mx-auto">
                    <p class="text-xl text-white text-center">Com CTI Insights, tudo fica mais fácil, todos os dados que importam para o seu crescimento a alguns cliques de distância</p>
                    <RouterLink to="/Login" class="bg-sky-700 px-3 py-1 block w-fit mx-auto rounded-full border-2 border-transparent mt-6
                    hover:bg-white 
                    hover:border-sky-700
                    hover:text-sky-700 transition duration-300">Acessar Sistema</RouterLink>
                </div>
            </section>
            <section id="sobre">
                <h2 class="flex justify-self-center font-bold text-xl">Sobre o CTI Insights</h2>
            </section>
            <section id="recursos"></section>
        </main>
        <footer id="contato" class="bg-black">
            <address class="not-italic text-center">
                <span>contato: </span>
                <a href="mailto:miguel.viana9@senaisp.edu.br">miguel.viana9@senaisp.edu.br</a>
            </address>
            <p class="text-white text-xs text-center">SENAI® 2026</p>
        </footer>
    </div>
</template>