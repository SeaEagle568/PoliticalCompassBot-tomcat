package com.libertaua.bot.util;

import com.libertaua.PoliticalCompassBot;
import com.libertaua.bot.Bot;
import com.libertaua.bot.entities.TelegramUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.UserProfilePhotos;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Class that is responsible for all operations with images
 */
@Component
public class ImageUtils {

    protected final BufferedImage[] achievments = new BufferedImage[6];
    protected BufferedImage compass;
    private final double COMPASS_CX = 408d;
    private final double COMPASS_CY = 441d;
    private final double COMPASS_MULT = 7.7d;
    private final double NOLAN_CX = 480d;
    private final double NOLAN_CY = 400d;
    private final double NOLAN_MULT = 5.0d;
    private final double COMPASS_R = 69d;
    private final double COMPASS_r = 55d;
    private final double NOLAN_R = 50d;
    private final double NOLAN_r = 40d;
    protected BufferedImage true_compass;
    public File ideologies_pic;
    public ArrayList<File> memes = new ArrayList<>();

    private Bot bot;
    @Autowired
    public void setBot(Bot bot) {
        this.bot = bot;
    }

    /**
     * Initialization - loading images from disc
     *
     */
    @PostConstruct
    public void init() {
        ideologies_pic = getResource("ideologies.png");
        File memesFolder = getResource("memes");
        for (final File fileEntry : Objects.requireNonNull(memesFolder.listFiles())) {
            if (!fileEntry.isDirectory()) memes.add(fileEntry);
        }
        memes.sort(Comparator.comparing(File::getName));

        try {
            compass = ImageIO.read(getResource("compass.png"));
            true_compass = ImageIO.read(getResource("true.jpg"));
            achievments[0] = ImageIO.read(getResource("ancap.jpg"));
            achievments[1] = ImageIO.read(getResource("ancom.jpg"));
            achievments[2] = ImageIO.read(getResource("authright.jpg"));
            achievments[3] = ImageIO.read(getResource("tankie.jpg"));
            achievments[4] = ImageIO.read(getResource("normie.jpg"));
            achievments[5] = ImageIO.read(getResource("gigachad.png"));
        } catch (IOException e) {
            String error = "Error uploading image";
            System.err.println(error);
            e.printStackTrace();
        }
    }

    private File getResource(String name) {
        String protocol = PoliticalCompassBot.class.getResource("PoliticalCompassBot.class").getProtocol();
        if (protocol.equals("jar")) {
            return new File(new File(".") + "/resources/BOOT-INF/classes/" + name);
        }
        try {
            return new File(ImageUtils.class.getClassLoader().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Private method to make a deep copy of BufferedImage
     * @param bi Original BufferedImage
     * @return   Copied BufferedImage
     */
    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null)
                .getSubimage(0, 0, bi.getWidth(), bi.getHeight());
    }

    /**
     * Method that returns a pair of Achievement image and its ID or null if no achievement
     * @param finalResults pair of doubles - standart results (0-100)
     * @param allZeros boolean if all answers were 0
     * @return ordered pair of Image file and ID
     */
    public Pair<File, Integer> getAchievment(String id, Pair<Double, Double> finalResults, boolean allZeros){
        BufferedImage achievment = null;
        Integer resultType = null;
        if (finalResults.first.intValue() == 100 && finalResults.second.intValue() == 100){
            achievment = deepCopy(achievments[0]);
            resultType = 0;
        }
        else if (finalResults.first.intValue() == 0 && finalResults.second.intValue() == 100){
            achievment = deepCopy(achievments[1]);
            resultType = 1;
        }
        else if (finalResults.first.intValue() == 100 && finalResults.second.intValue() == 0){
            achievment = deepCopy(achievments[2]);
            resultType = 2;
        }
        else if (finalResults.first.intValue() == 0 && finalResults.second.intValue() == 0){
            achievment = deepCopy(achievments[3]);
            resultType = 3;
        }
        else if (allZeros){
            achievment = deepCopy(achievments[4]);
            resultType = 4;
        }
        else if (finalResults.first.intValue() == 50 && finalResults.second.intValue() == 50){
            achievment = deepCopy(achievments[5]);
            resultType = 5;
        }
        else return null;
        File tempFile = new File(id + "/tempImage.png");
        tempFile.getParentFile().mkdirs();
        try {
            ImageIO.write(achievment, "png", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Pair<>(tempFile, resultType);
    }
    /**
     * Oh, that's a shitty one
     * A methods that generates image with dot at right place from final user results
     * Saves temp file btw
     * @param finalResults Pair<Double, Double> coordinates from (0,0) to (100, 100) representing results
     * @return             A temporary file with resulting image
     */
    public File getResultsImage(String id, BufferedImage userPic, Pair<Double, Double> finalResults, boolean trueCompass) {
        BufferedImage finalCompass;
        finalResults.first -= 50;
        finalResults.second -= 50;
        if (trueCompass){

            if (userPic == null) {
                finalCompass = getCompassWithDot(deepCopy(true_compass),
                        rotateCoords(finalResults, 135),
                        NOLAN_CX, NOLAN_CY, NOLAN_R, NOLAN_r, NOLAN_MULT);
            }
            else {
                finalCompass = getCompassWithPic(userPic,
                        deepCopy(true_compass),
                        rotateCoords(finalResults, 135),
                        NOLAN_CX, NOLAN_CY, NOLAN_R, NOLAN_MULT);
            }
        }

        else {
            if (userPic == null) {
                finalCompass = getCompassWithDot(deepCopy(compass),
                        finalResults,
                        COMPASS_CX, COMPASS_CY, COMPASS_R, COMPASS_r, COMPASS_MULT);
            }
            else {
                finalCompass = getCompassWithPic(userPic,
                        deepCopy(compass),
                        finalResults,
                        COMPASS_CX, COMPASS_CY, COMPASS_R, COMPASS_MULT);
            }
        }
        finalResults.first += 50;
        finalResults.second += 50;
        //Creating a temp file and saving result there
        File tempFile = new File(id + "/tempImage.png");
        tempFile.getParentFile().mkdirs();
        try {
            ImageIO.write(finalCompass, "png", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    /**
     * Rotate coordinates by multiplication on rotation matrix
     *
     * @param input pair of doubles in format [-50, 50]
     * @param degree degree (not radians)
     * @return pair of transformed results
     */
    private static Pair<Double, Double> rotateCoords(Pair<Double, Double> input, double degree){
        Pair<Double, Double> result = new Pair<>(0d,0d);
        input.second = -input.second;
        result.first = Math.cos(Math.toRadians(degree)) * input.first + (-Math.sin(Math.toRadians(degree))) * input.second;
        result.second = Math.sin(Math.toRadians(degree)) * input.first + (Math.cos(Math.toRadians(degree))) * input.second;
        input.second = -input.second;
        result.first = -result.first;
        result.second = -result.second;
        return result;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, double targetWidth, double targetHeight) {
        BufferedImage resizedImage = new BufferedImage((int)Math.round(targetWidth), (int)Math.round(targetHeight), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, (int)Math.round(targetWidth),  (int)Math.round(targetHeight), null);
        graphics2D.dispose();
        return resizedImage;
    }

    /**
     * Crop image to circle
     * @param original original image
     * @return new image
     */
    private BufferedImage getCircle(BufferedImage original){
        int width = original.getWidth();
        BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circleBuffer.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, width, width));
        g2.drawImage(original, 0, 0, width, width, null);
        g2.dispose();
        return circleBuffer;
    }

    /**
     * Returns new compass with userpic
     * @param userPic userpic image
     * @param compass blank compass image
     * @param finalResults results on compass
     * @param centerX center of compass X
     * @param centerY center of compass Y
     * @param R radius of resulting circle
     * @param multiplier multiplier for coordinates to fit compass
     * @return resulting image
     */
    private BufferedImage getCompassWithPic(BufferedImage userPic, BufferedImage compass, Pair<Double, Double> finalResults, double centerX, double centerY, double R, double multiplier) {
        BufferedImage finalPic = resizeImage(getCircle(userPic),R, R);
        Graphics2D graphics2D = compass.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.drawImage(finalPic,
                (int) (centerX - R/2 + Math.round(finalResults.first * multiplier)),
                (int) (centerY - R/2 + Math.round(finalResults.second * multiplier)),
                null
        );
        graphics2D.dispose();
        return compass;
    }

    /**
     * Creates new compass with black/red dot
     *
     * @param compass blank compass image
     * @param finalResults results on compass
     * @param centerX center of compass X
     * @param centerY center of compass Y
     * @param R radius of outer circle (black)
     * @param r radius of inner circle (red)
     * @param multiplier multiplier for coordinates to fit compass
     * @return resulting image
     */
    private BufferedImage getCompassWithDot(BufferedImage compass, Pair<Double, Double> finalResults, double centerX, double centerY, double R, double r, double multiplier)
    {
        Graphics2D graphics2D = compass.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setColor(Color.BLACK); //Outer circle
        graphics2D.fillOval(
                (int) (centerX - R/2 + Math.round(finalResults.first * multiplier)),
                (int) (centerY - R/2 + Math.round(finalResults.second * multiplier)),
                (int) R,
                (int) R
        );
        graphics2D.setColor(Color.RED); //Inner circle
        graphics2D.fillOval(
                (int) (centerX - R/2 + Math.round(finalResults.first * multiplier) + (R-r) / 2),
                (int) (centerY - R/2 + Math.round(finalResults.second * multiplier) + (R-r) / 2),
                (int) r,
                (int) r
        );
        graphics2D.dispose();
        return compass;
    }

    /**
     * Gets userpic from telegram user
     * @param currentUser TelegramUser
     * @return image of userpic
     */
    public BufferedImage getUserPic(TelegramUser currentUser) {
        GetUserProfilePhotos getUserProfilePhotos = new GetUserProfilePhotos(Long.parseLong(currentUser.getUserId()), 0, 1);
        try {
            UserProfilePhotos userPhotos = bot.execute(getUserProfilePhotos);
            List<List<PhotoSize>> photoes = userPhotos.getPhotos();
            String fileId = photoes.get(0).get(0).getFileId();
            GetFile fileGetter = new GetFile();
            fileGetter.setFileId(fileId);
            File photoFile = bot.downloadFile(bot.execute(fileGetter));
            return ImageIO.read(photoFile);
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return null;

    }
}
