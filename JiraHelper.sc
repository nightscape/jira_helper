import $file.Model, Model._
import $file.JiraApiWrapper, JiraApiWrapper._
import $file.TemplateParser, TemplateParser._

val standardIn = System.console

def fromEnvOrInput(varName: String, hideInput: Boolean = false) = {
  sys.env.get(varName).getOrElse(
    {
      print(s"$varName> ")
      if (hideInput)
        standardIn.readPassword().mkString
      else
        standardIn.readLine
    }
  )
}

implicit val client = createJiraClient(fromEnvOrInput("JIRA_URL"), fromEnvOrInput("JIRA_USER"), fromEnvOrInput("JIRA_PASS", hideInput = true))

def createSubtasks(issueId: String, subtasks: Seq[Model.Subtask]): Unit = {
  val issue = client.issue(issueId)
  subtasks.foreach { subtask =>
    val status = subtask.status.flatMap(client.findStatus)
    issue.findSubtask(subtask.summary).map { existingSubtask =>
      println(s"""Existing subtask "${existingSubtask.getSummary}" in ${issue.getKey}""")
    }.getOrElse {
      val createdSubtask = issue.createSubtask(subtask.summary, subtask.description.getOrElse(""), status = status)
      println(s"""Created subtask "${createdSubtask.getSummary}" in ${issue.getKey}""")
    }
  }

}
@main
def createSubtasksFromYaml(issueId: String, templateUrl: String) = {
  createSubtasks(issueId, readTemplateFromUrl(templateUrl).subtasks)
}

@main
def help(): Unit = {
  println(scala.io.Source.fromFile("README.md").mkString)
}
