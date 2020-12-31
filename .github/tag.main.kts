@file:Repository("https://jcenter.bintray.com")  // For turtle
@file:DependsOn("com.lordcodes.turtle:turtle:0.2.0")

import com.lordcodes.turtle.shellRun
import java.io.File

//val webhookUrl = System.getenv("WEBHOOK_URL")
val repo = System.getenv("GITHUB_REPOSITORY")

var githubTag: String = System.getenv("GITHUB_REF")

if (githubTag.contains("/")) {
    githubTag = githubTag.split("/").last()
}

println("Current tag: $githubTag")

if (githubTag.contains("v")) {
    githubTag = githubTag.split("v", limit = 2).last()
}

val tags = shellRun("git", listOf("tag")).trim().split("\n")

val commits = if (tags.size < 2) {
    println("No previous tags, using all branch commits.")

    val branch = shellRun("git", listOf("branch", "--show-current"))

    shellRun("git", listOf("log", "--format=oneline", "--no-color", branch))
} else {
    val previousTag = tags.takeLast(2).first()

    println("Previous tag: $previousTag")

    shellRun("git", listOf("log", "--format=oneline", "--no-color", "$previousTag..HEAD"))
}.split("\n").map {
    val split = it.split(" ", limit = 2)
    val commit = split.first()
    val message = split.last()

    "* [$commit](https://github.com/$repo/commit/$commit) $message"
}

println("Commits: ${commits.size}")

val file = File("release.md")

file.writeText(
    "# Release $githubTag\n\n" +

            commits.joinToString("\n") + "\n\n" +

            "**This release description was generated automatically, and may be updated later.**"
)

print("File written: release.md")
