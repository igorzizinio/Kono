package me.igorunderplayer.kono.domain.card

enum class EquipmentSlot(
    val index: Int,
    val displayName: String,
    val icon: String,
    val aliases: List<String>
) {
    WEAPON(0, "Arma", "⚔️", listOf("arma", "weapon", "espada", "sword")),
    BOOTS(1, "Botas", "👢", listOf("botas", "bota", "boots", "boot")),
    ARMOR(2, "Armadura", "🛡️", listOf("armadura", "armor", "armour", "roupa", "cloth")),
    SECONDARY(
        3,
        "Secundário",
        "🔮",
        listOf("secundario", "secundário", "secondary", "escudo", "shield", "talisman", "talismã")
    ),
    TRINKET(4, "Acessório", "💍", listOf("acessorio", "acessório", "trinket", "anel", "ring", "accessory"));

    companion object {
        fun fromIndex(index: Int): EquipmentSlot? = entries.firstOrNull { it.index == index }

        fun fromInput(input: String): EquipmentSlot? {
            val n = input.toIntOrNull()
            if (n != null) return fromIndex(n - 1)
            return entries.firstOrNull { slot ->
                slot.name.equals(input, ignoreCase = true) ||
                        slot.aliases.any { it.equals(input, ignoreCase = true) }
            }
        }
    }
}
