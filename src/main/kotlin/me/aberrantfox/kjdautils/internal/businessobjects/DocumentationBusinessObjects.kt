package me.aberrantfox.kjdautils.internal.businessobjects


val HEADER_DATA = CommandData("Commands", "Arguments", "Description")

data class CategoryDocs(val name: String, val docString: String)

data class CommandData(val name: String, val args: String, val description: String) {
    fun format(format: String) = String.format(format, name, args, description)
}

data class CommandsOutputFormatter(
        var longestName: Int = HEADER_DATA.name.length,
        var longestArgs: Int = HEADER_DATA.args.length,
        var longestDescription: Int = HEADER_DATA.description.length) {
    fun generateFormatString() = "| %-${longestName}s | %-${longestArgs}s | %-${longestDescription}s |"
}
