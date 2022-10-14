# (1)
FROM maven:3-openjdk-17-slim as build
# (2)
RUN useradd -m edurbs
# (3)
WORKDIR /usr/src/scriptureearth2meps/
# (4)
RUN chown edurbs:edurbs /usr/src/scriptureearth2meps/
# (5)
USER edurbs
# (6)
COPY --chown=edurbs pom.xml ./
# (7)
RUN mvn dependency:go-offline -Pproduction
# (8)
COPY --chown=edurbs:edurbs src src
COPY --chown=edurbs:edurbs frontend frontend
COPY --chown=edurbs:edurbs package.json ./
COPY --chown=edurbs:edurbs package-lock.json* pnpm-lock.yaml* webpack.config.js* ./
# (9)
RUN mvn clean package -DskipTests -Pproduction
# (10)
FROM openjdk:17-jdk-slim
# (11)
COPY --from=build /usr/src/scriptureearth2meps/target/*.jar /usr/scriptureearth2meps/scriptureearth2meps.jar
# (12)
RUN useradd -m edurbs
# (13)
USER edurbs
# (14)
EXPOSE 8080
# (15)
CMD java -jar /usr/scriptureearth2meps/scriptureearth2meps.jar