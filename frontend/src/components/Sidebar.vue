<template>
    <aside class="flex flex-col w-60 min-h-screen bg-linear-to-br from-blue-800 via-slate-800 to-sky-800 p-4">
        <div>
            <img src="/cti-logo.png" alt="CTI" class="h-20 block mx-auto mb-8">
            <nav class="flex flex-col gap-2">
                <RouterLink to="/dashboard" active-class="bg-linear-to-br from-sky-800 via-slate-800 to-blue-800 hover:bg-sky-700!" class="text-center text-white rounded-lg px-3 py-2 hover:bg-slate-700">Dashboard</RouterLink>
                <RouterLink to="/upload" active-class="bg-linear-to-br from-sky-800 via-slate-800 to-blue-800 hover:bg-sky-700!" class="text-center text-white rounded-lg px-3 py-2 hover:bg-slate-700">Upload</RouterLink>
                <RouterLink to="/usurios" active-class="bg-linear-to-br from-sky-800 via-slate-800 to-blue-800 hover:bg-sky-700!" class="text-center text-white rounded-lg px-3 py-2 hover:bg-slate-700">Usuários</RouterLink>
            </nav>
        </div>
        <button class="bg-red-600 rounded-xl text-center px-3 py-1 cursor-pointer hover:bg-red-500 transition duration-300 mt-auto">Sair</button>
    </aside>
</template>