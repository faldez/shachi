package com.faldez.shachi.data.util

fun String.isManualSearchTags() = this.contains(Regex("[{}~]"))