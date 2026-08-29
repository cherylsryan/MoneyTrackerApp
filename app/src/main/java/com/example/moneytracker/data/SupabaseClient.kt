package com.example.moneytracker.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    private const val SUPABASE_URL = "https://lurvfjswzqawdznqrvvy.supabase.co"
    private const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_KAsMFO3aRMm3vYNjs6ROSQ_Hcebuw4b"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Postgrest)
        install(Storage)
    }
}