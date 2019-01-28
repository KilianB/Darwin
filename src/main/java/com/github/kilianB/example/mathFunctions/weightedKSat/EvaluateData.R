

setwd("C:\\Users\\Kilian\\git\\imageRaster")

library(ggplot2)


generateChart <- function(df){

  # On data  
  smallerTransIndex <- min(which(df$`Solvable (%)` < 0.5))
  transitionPoint <- (df[smallerTransIndex,]$`Clauses / Vars` +  df[smallerTransIndex-1,]$`Clauses / Vars`)/2

  # On fitted 
  fitted <- predict(loess(df$`Solvable (%)` ~ df$`Clauses / Vars`,df), df$`Clauses / Vars`)
  smallerTransIndexFitted <- min(which(fitted < 0.5))
  transitionPointFitted <- (df$`Clauses / Vars`[smallerTransIndexFitted] + df$`Clauses / Vars`[smallerTransIndexFitted-1])/2
  
  #The fit should only focus on the actual cubic poly
  dataOfInteres <- df #[df$`Solvable (%)` < 1,]
  
  g <- ggplot(df, aes(x = `Clauses / Vars`,y = `Solvable (%)`))  +
    geom_smooth(data = dataOfInteres, aes(x = `Clauses / Vars`,y = `Solvable (%)`),method='loess',span=0.5) +
    geom_point() + 
    geom_vline(xintercept = transitionPoint, colour = "orange") + 
    geom_vline(xintercept = transitionPointFitted , colour = "blue") +
    ggtitle(paste(df[1,]$k," Sat")) + 
    xlab("Clauses / Vars(15)") + 
    annotate(geom ="text" , label = "Each Point was computed by generating 600 random problems", x = 0, y = 0, hjust = 0) + 
    annotate(geom ="text" , label = paste("Transition Point:",format(round(transitionPoint, 2), nsmall = 2)), x = 0, y = 0.06, hjust = 0, colour = "orange") + 
    annotate(geom ="text" , label = paste("Transition Point Fitted:",format(round(transitionPointFitted, 2), nsmall = 2)), x = 0, y = 0.03, hjust = 0, colour = "blue")    
  
  return(g)
}

sat.2 <- read.csv("WeightedKSat2.csv",header=TRUE,sep=",")
sat.3 <- read.csv("WeightedKSat3.csv",header=TRUE,sep=",")
sat.5 <- read.csv("WeightedKSat5.csv",header=TRUE,sep=",")
sat.6 <- read.csv("WeightedKSat6.csv",header=TRUE,sep=",")


colnames(sat.2) <- c("k","Clauses / Vars","Solved","Unsolved","Solvable (%)")
colnames(sat.3) <- c("k","Clauses / Vars","Solved","Unsolved","Solvable (%)")
colnames(sat.5) <- c("k","Clauses / Vars","Solved","Unsolved","Solvable (%)")
colnames(sat.6) <- c("k","Clauses / Vars","Solved","Unsolved","Solvable (%)")


pplot.2 <- generateChart(sat.2)
pplot.3 <- generateChart(sat.3)
pplot.5 <- generateChart(sat.5)
pplot.6 <- generateChart(sat.6)

pplot.2
pplot.3
pplot.5
pplot.6

colnames(sat.5) <- c("k","clauses","solve","unsolved","solvable")

fitted <- predict(loess(df$`Solvable (%)` ~ df$`Clauses / Vars`,df), df$`Clauses / Vars`)

fitted

min(which(fitted < 0.5))

df$`Clauses / Vars`[min(which(fitted < 0.5))]

smallerTransIndexFitted <- min(which(fitted < 0.5))
transitionPointFitted <- (fitted[smallerTransIndexFitted] + fitted[smallerTransIndexFitted-1])/2
print(transitionPointFitted)

