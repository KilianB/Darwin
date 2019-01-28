

Sys.setenv('MAPBOX_TOKEN' = 'sk.eyJ1IjoicGF0ZXgxIiwiYSI6ImNqbGg4bDlhczFiZmUzdnFvanFvM215NXIifQ.Kf6vcN-gZSu4RcVddfwNig')


## Tools used -> Java prog to create anyimations
## Orca to export pplotly images without restriction
## Imagemagick to create the gif  magick convert -delay 5 -loop 0 *.png -layers optimize-plus animatedQuality.gif
## https://ezgif.com/optimize/ for future file size reduction 10% compression level


library(ggplot2)
library(plotly)
library(plyr)
library(dplyr)
library(reshape2)
library(ggplot2)
library(grid)
library("gridExtra")


setwd("C:\\Users\\Kilian\\git\\imageRaster")

generationStats <- read.csv("generationStats.csv",header=FALSE,sep=";")
colnames(generationStats) <- c('generation','min','max','average','sum')

populationData <- read.csv("populationDetails.csv",header=FALSE,sep=";")
colnames(populationData) <- c('generation','fitness','x','x2')

#################


scatter <- plot_ly(data = populationData,x = ~generation,   y = ~fitness, type = 'scatter')
scatter




plot_ly(data = generationStats, x = ~generation, y=~min, type = 'scatter')



popImage <- read.csv("Pop.csv",header=FALSE,sep=";")
sumImage <- read.csv("Sum.csv",header=FALSE,sep=";")
colnames(sumImage) <- c('generation','min','max','average','sum')

p1 <- plot_ly(data = sumImage, x = ~generation, y=~min, type = 'scatter') %>%
  add_trace(data = sumImage, x = ~generation,y= ~max) %>%
  add_trace(data = sumImage, x = ~generation,y= ~average) %>%
  layout(
    yaxis = list(
      range = c(-50000,max(sumImage$min)*1.2),
      title = "Generation"
    ),
    xaxis = list(
      title = "Fitness"
    )
  )
 
p1
p2


rastrigin <- function(x1,x2){
  return (20 + x1^2 + x2^2 - 10*(cos(2*pi*x1) + cos(2*pi*x2)))
}

index <- seq(-10,10,0.1)
df <- expand.grid(x = index,y = index)
df$rastrigin <- rastrigin(df$x,df$y)

### We need to convert d1 to wide format?
dfAsMatrix <- dcast(df, x~y, value.var = "rastrigin")
rownames(dfAsMatrix) <- dfAsMatrix[,1]
dfAsMatrix <- data.matrix(dfAsMatrix[,-1])


rosenbrock <- function(x1, x2){
  a <- 1
  b <- 100
  return ((a-x1)^2 + b * (x2 - (x1^2))^2)
}

indexSmall <- seq(-3,3,0.05)

df.rosenbrock <- expand.grid(x = indexSmall,y = indexSmall)
df.rosenbrock$rosenbrock <- rosenbrock(df.rosenbrock$x,df.rosenbrock$y)

df.rosenbrockAsMatrix <- dcast(df.rosenbrock, x~y, value.var = "rosenbrock")
rownames(df.rosenbrockAsMatrix) <- df.rosenbrockAsMatrix[,1]
df.rosenbrockAsMatrix <- data.matrix(df.rosenbrockAsMatrix[,-1])

plot_ly(data = df.rosenbrock, x = ~ x, y = ~y, z = ~rosenbrock, type = "contour") %>%
  colorbar(title = "Value") %>%
  layout(
    xaxis = list(
      range = c(-3,3)
    ),
    yaxis = list(
      range = c(-3,3)
    )
  )


rosenbrockLog <- log10(df.rosenbrockAsMatrix)

zeroIndex <- which.min(rosenbrockLog)

rosenbrockLog[zeroIndex] <- 600000
rosenbrockLog[zeroIndex] = min(rosenbrockLog)*.08

cam.zoom <- 2



# orca is buggy. For the meantime adjust the wd

setwd("C:\\Users\\Kilian\\git\\imageRaster\\images\\rosenbrock")

maxIndex <- 2*pi
frames <- 253
steps <- maxIndex / frames

#Don't double last and first frame
for(i in seq(0,maxIndex,steps)){
  surfacePlot <- plot_ly(z = ~rosenbrockLog,colors = colorRamp(c("blue","cornflowerblue","chartreuse","yellow","orange","red"))) %>% 
    add_surface() %>% layout(title = "Rosenbrock",
                             scene = list(
                               xaxis = list(title = "X",
                                            showticklabels = FALSE),
                               yaxis = list(title = "X2",
                                            showticklabels = FALSE),
                               zaxis = list(title = "Value",
                                            showticklabels = FALSE),
                               camera = list(eye = list(x = cos(i)*cam.zoom,y = sin(i)*cam.zoom, z=1),
                                             center = list(x = 0,
                                                           y = 0,
                                                           z = 0
                                             )
                               )  )
    ) %>%   colorbar(title = "Value")
  

  outFile <- paste(paste0(formatC(round(i*1000),width=5,flag="0")),"png", sep=".")

  orca(surfacePlot,
       file = outFile,
       width = 650, #1200
       height = 560, #1050
       format = "png",
       scale = 1)
}
#Reset
setwd("C:\\Users\\Kilian\\git\\imageRaster")


contour1 <- plot_ly(z = ~rosenbrockLog, type = "contour") %>%
  colorbar(title = "Value")
contour1


##We need filenames with leading zeros or the images are not picked up in the correct order

maxNumCharacters <- floor(log(max(populationData$generation),base = 10))+1

for(i in unique(populationData$generation)){
  singleGen <- populationData[populationData$generation == i,]
  singleBestAnswer <- singleGen[which.min(singleGen$fitness),]
  g1 <- ggplot(df, aes(x,y,z=rastrigin)) + geom_raster(aes(fill = rastrigin)) + 
    scale_fill_distiller(palette="RdYlGn")  + #Spectral
    geom_contour(bins = 15, colour = "black", lineend = "butt", linejoin = "round") +
    geom_point(data=singleGen,alpha = 9/10,colour = "black", fill = "white", size = 4, stroke = 1,
               shape = 21,
               aes(x =x,y=x2,z=fitness)) + 
    geom_point(data = singleBestAnswer,colour = "black", fill = "cadetblue1", size = 4, stroke = 1,
               shape = 21,
               aes(x =x,y=x2,z=fitness))+
    xlim(-10, 10) + ylim(-10, 10) +
    theme(
      legend.position="none",
      panel.background = element_blank(),
      axis.line.x.bottom = element_line(size = 0.4),
      axis.line.y.left = element_line(size = 0.4),
      axis.text = element_text(size = 9),
      axis.text.y.left = element_text(margin = margin(5,15,0,0)),
      axis.text.x.bottom = element_text(margin = margin(15,0,5,0)),
      axis.title = element_text(size = 11),
      axis.ticks = element_blank(),
      panel.grid.major = element_line(colour="#e2e2e2", size=0.5)
    )
  
  singleStat <- generationStats[generationStats$generation <= i,]
  singlePop <- populationData[populationData$generation <= i,]
 
  g2 <- ggplot(singleStat, aes(generation,min)) +
    xlim(0,max(generationStats$generation)*1.1) + 
    ylim(0,max(generationStats$max)*1.1) + 
    geom_point(data = singlePop, aes(x = generation, y = fitness)) +
    geom_line(data = singleStat, aes(x = generation, y = average) , color="red", size = 1) +
    geom_point(color="cadetblue1") +
    xlab("Generation") +
    ylab("Fitness") +
  theme(
    panel.background = element_blank(),
    axis.line.x.bottom = element_line(size = 0.4),
    axis.line.y.left = element_line(size = 0.4),
    axis.text = element_text(size = 9),
    axis.text.y.left = element_text(margin = margin(5,15,0,0)),
    axis.text.x.bottom = element_text(margin = margin(15,0,5,0)),
    axis.title = element_text(size = 11),
    axis.ticks = element_blank(),
    panel.grid.major = element_line(colour="#e2e2e2", size=0.5)
  )
    
    grob <- arrangeGrob(g1, g2, nrow = 1,
                top =  textGrob(paste("Generation: ",i),gp=gpar(fontsize=15,font=1,col = "#AAAAAA")))

   ggsave(plot = grob, filename= paste0("images/temp",formatC(i,width=maxNumCharacters,flag="0"),".png"),width=16, height = 8, dpi = 100)
}








##GG anim
g1 <- ggplot(df, aes(x,y,z=rastrigin)) + geom_raster(aes(fill = rastrigin)) + 
  scale_fill_distiller(palette="RdYlGn")  + #Spectral
  geom_contour(bins = 15, colour = "black", lineend = "butt", linejoin = "round") +
  geom_point(data= populationData, alpha = 9/10,colour = "black", fill = "white", size = 4, stroke = 1,
             shape = 21, aes(x = x, y = x2, z = fitness)) +
  transition_states(
    states = generation,
    transition_length = 1,
    state_length = 2
  ) + 
  labs(title = 'Generation: {closest_state}', x = 'GDP per capita', y = 'life expectancy')

animate(g1, detail = 2, nframes = length(unique(populationData$generation))*5)


#for windows magick convert -delay 40 -loop 0 *.png animated.gif

library(purrr) # for mapping over a function
library(magick) # this is call to animate/read pngs

fileInfo <- file.info(list.files(path = "images/", pattern = "*.png", full.names = T))
fileInfo <- fileInfo[with(fileInfo, order(as.POSIXct(mtime))), ]
rownames(fileInfo) %>% 
  map(image_read) %>% # reads each path file
  image_join() %>% # joins image
  image_animate(fps=10) %>% # animates, can opt for number of loops
  image_write("ndwi_aug_hgm.gif") 



# ggplot() + geom_raster(data = df, aes(x,y,z=rastrigin,fill = rastrigin)) + 
#   scale_fill_distiller(palette="RdYlGn", na.value="white")  + #Spectral
#   geom_contour(bins = 10, colour = "black", lineend = "butt", linejoin = "round") +
#   geom_point(data = populationData,aes(x =x,y=x2,z=fitness))+ 
#   xlim(-5, 5) + ylim(-5, 5) +
#   labs(title = 'Generation: {frame_time}') + 
#   theme_classic() +
#   transition_time(generation) + 
#   transition_states(generation,3,1) +
#   ease_aes('linear')




heatmapPlot <- 
  plot_ly(x=df$x,y= df$y,z=df$rastrigin, type = "heatmap" ,colors = colorRamp(c("blue","cornflowerblue","chartreuse","yellow","orange","red"))) %>% 
  layout(title = "Rastrigin mapped to 2D")

surfacePlot <- plot_ly(z = ~dfAsMatrix,colors = colorRamp(c("blue","cornflowerblue","chartreuse","yellow","orange","red"))) %>% 
  add_surface() %>% layout(title = "Rastrigin",
                           scene = list(
                             xaxis = list(title = "X"),
                             yaxis = list(title = "X2"),
                             zaxis = list(title = "Value")
                           )) %>%   colorbar(title = "Value")

contour1 <- plot_ly(z = ~dfAsMatrix, type = "contour") %>%
  colorbar(title = "Value")


plot_ly(data = df, x = ~ x, y = ~y, z = ~rastrigin, type = "contour") %>%
  colorbar(title = "Value") %>%
  add_trace(data = gen0,x=~x,y=~x2,type = "scatter", mode = "markers",
            marker = list(size = 10,
                          
                          color = 'rgba(255, 182, 193, .7)',
                          line = list(color = 'rgba(152, 0, 0, 1)',
                                      width = 2))
            ) %>%
  layout(
    xaxis = list(
      range = c(-5,5)
    ),
    yaxis = list(
      range = c(-5,5)
    )
  )


scatterGen <- plot_ly(data = gen0,x=~x,y=~x2,type = "scatter", mode="markers" )

add_contour(scatterGen,data = df, x = ~ x, y = ~y, z = ~rastrigin, type = "contour")


genTotal <- populationData[1:100,]
genTotal$generation <- factor(genTotal$generation, ordered = TRUE)

dput(genTotal,file = "gen.dat")
dput(df,file = "df.dat")


unique(genTotal$generation)

levels(genTotal$generation) <- 1
  

  

genTotal$generation


###Attempt animation

plot_ly(data = genTotal,x=~x,y=~x2,type = "scatter", mode = "markers",
        frame = ~generation,marker = 
          list(size = 10,
               color = 'rgba(255, 182, 193, .7)',
               line = list(color = 'rgba(152, 0, 0, 1)',width = 2))
        ) %>% layout(
  xaxis = list(
    range = c(-5,5)
  ),
  yaxis = list(
    range = c(-5,5)
  )
)
        


fig <- plot_ly(data = df, x = ~ x, y = ~y, z = ~rastrigin, type = "contour") %>%
  colorbar(title = "Value") %>%
  add_trace(data = genTotal,x=~x,y=~x2,type = "scatter", mode = "markers",
            inherit = FALSE,
            frame = ~generation,
            marker = list(size = 10,
                  color = 'rgba(255, 182, 193, .7)',
                  line = list(color = 'rgba(152, 0, 0, 1)',width = 2))
            
  ) %>% layout(
    xaxis = list(
      range = c(-5,5)
    ),
    yaxis = list(
      range = c(-5,5)
    )
  )
fig
fig %>% toWebGL()

htmlwidgets::saveWidget(as_widget(fig), "graph.html")

offline.plot(fig, image='png', image_filename=chart_name, filename=file_name, auto_open=False)


  layout(
    xaxis = list(
      range = c(-5,5)
    ),
    yaxis = list(
      range = c(-5,5)
    )
  )


str(genTotal)

genTotal$generation <- as.factor(genTotal$generation)

heatmapPlot
surfacePlot
contour1

#options(browser = 'false')
api_create(p, filename = "r-docs-midwest-boxplots")
