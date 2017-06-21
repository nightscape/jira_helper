case class Subtask(summary: String, description: Option[String] = None, status: Option[String] = None)
case class Issue(subtasks: Seq[Subtask])