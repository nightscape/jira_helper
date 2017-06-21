import coursier.MavenRepository

interp.repositories() ++= Seq(
  MavenRepository(
    "https://m2proxy.atlassian.com/repository/public"
  )
)