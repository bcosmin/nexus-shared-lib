1. Construcție și Calitatea Codului (Code Quality & Security)
gitCheckout.groovy (sau scmCheckout)

Un wrapper standardizat pentru checkout scm, care știe să facă shallow clone, să ignore anumite fișiere sau să gestioneze credențiale comune.

sonarqubeScan.groovy

Integrare standardizată pentru analiza statică a codului (SonarQube / SonarCloud), cu setarea automată a quality gates și a parametrilor specifici în funcție de limbaj.

trivyScan.groovy (sau alt scanner de securitate)

Pentru scanarea vulnerabilităților din cod, imagini Docker sau dependențe (npm, maven, pip) înainte de build.

2. Containerizare și Regetry (Docker & OCI)
dockerBuildPush.groovy

Un pas esențial care preia un Dockerfile, construiește imaginea, îi aplică tag-uri dinamice (bazate pe branch, Git commit SHA, numărul build-ului din Jenkins) și o urcă într-un registry privat.

3. Deployment și Infrastructură (Cloud & Artifacte)
deployHelm.groovy (sau helmUpgrade)

Pentru livrarea aplicațiilor în clustere Kubernetes folosind Helm charts. Gestionează valori dinamice, namespace-uri, dry-run-uri și rollback-uri automate în caz de eșec.

Relația cu fișierele tale (uploadToS3.groovy, deployArtifactsS3.groovy, deployArtifactsJfrog.groovy)

Diferența fină: uploadToS3 poate fi un utilitar generic (gen un wrapper peste comanda aws s3 cp sau plugin-ul de S3), în timp ce deployArtifactsS3 / deployArtifactsJfrog sunt pași orientati pe business (ex: împachetează un folder/arhiva, îi aplică un naming convention strict și îl urcă în repository-ul dedicat de artifacte).

4. Utilitare și Gestionarea Fluxului
withVault.groovy (sau integrare cu HashiCorp Vault / AWS Secrets Manager)

Un pas care simplifică preluarea secretelor și injectarea lor în mediu sau în variabilele de build, fără a le expune în logs.

safeRollback.groovy

Un mecanism standardizat care știe să readucă o aplicație la starea anterioară dacă un pas critic de deployment eșuează.
