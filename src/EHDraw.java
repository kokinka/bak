/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EHDraw extends Application {

    static EvolutionTree strom = new EvolutionTree();
    static GraphicsContext gc;
    static Stage primaryStage;
    static boolean gui = true;// -nogui to turn off
    static boolean optimize = false; // -opt
    static boolean export_greedy = false; //-exportgreedy
    static boolean export_ilp = false; //-exportilp
    static boolean export_minimized = false; //-exportminimized
    static boolean draw_svg = false; // -drawsvg
    static boolean export_crossings = false; //-exportcrossingscount
    static String input; //-input:input.history
    static String svg_output = "output.svg";
    static String ilp_output = "output.lp";
    static String greedy_output = "output.greedy";
    static String minimized_output = "minimized.history";
    static String crossings_output = "crossings.txt";
    static String load_settings = null; //-settings:filename
    static String load_lp = null;
    static ComboBox pickGene = new ComboBox();
    static Button removeMeta;
    static ComboBox drawC;
    static ComboBox highlightedC;
    static ComboBox transparentC;
    static TextField lineWF;
    static TextField nameF;
    static ColorPicker colorP;
    static Button useDefault;

    static Stage geneSettingsStage = new Stage();
    static Group group = new Group();
    static DrawFactory drawfact = new FXSelectableDrawFactory(group, geneSettingsStage, pickGene);

    //static Settings s=new Settings();
    //Scanner in = new Scanner(System.in);
    @Override
    public void start(Stage _primaryStage) throws FileNotFoundException, IOException {
        primaryStage = _primaryStage;

        BorderPane root = new BorderPane();
        final Canvas cnv = new Canvas(Settings.width, Settings.height);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu settingsMenu = new Menu("Settings");
        menuBar.getMenus().addAll(fileMenu, settingsMenu);

        Canvas canvas = new Canvas(Settings.width, Settings.height);
        group.getChildren().add(canvas);
        root.setCenter(group);
        root.setTop(menuBar);
        Scene scene = new Scene(root, Settings.width, Settings.height);

        SceneGestures sceneGestures = new SceneGestures(group);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        scene.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                double delta = 1.1;
                double min_scale = 0.1;
                double max_scale = 10;
                double scale;

                if (event.getDeltaY() < 0) {
                    scale = Math.max(Settings.scale / delta, min_scale);
                } else {
                    scale = Math.min(Settings.scale * delta, max_scale);
                    if (Settings.real_height > 10000) scale = Settings.scale;

                }

                Settings.scale = scale;
                canvas.setWidth(Settings.width * Settings.scale);
                strom.calcScale();
                strom.calcRealHeight();
                canvas.setHeight(Settings.real_height);
                draw();
                event.consume();
            }
        });

        primaryStage.setTitle(Settings.title);
        primaryStage.setScene(scene);

        primaryStage.show();

        gc = canvas.getGraphicsContext2D();


        MenuItem openItem = new MenuItem("Open");
        MenuItem optimizeItem = new MenuItem("Optimize");
        MenuItem exportILPItem = new MenuItem("Export ILP");
        MenuItem importILPItem = new MenuItem("Import ILP");
        MenuItem saveIMGItem = new MenuItem("Save Image");
        MenuItem redrawItem = new MenuItem("Redraw");
        MenuItem crossingItem = new MenuItem("Minimize Crossing");
        MenuItem exportHistoryItem = new MenuItem("Export History");
        fileMenu.getItems().addAll(openItem, saveIMGItem, optimizeItem, exportILPItem,
                importILPItem, redrawItem, crossingItem, exportHistoryItem);

        openItem.setOnAction(new OpenHandler(primaryStage, canvas));
        optimizeItem.setOnAction(new OptimizeHandler());
        exportILPItem.setOnAction(new exportILPHandler());
        importILPItem.setOnAction(new importILPHandler());
        saveIMGItem.setOnAction(new SaveImgHandler());
        redrawItem.setOnAction(new redrawHandler());
        crossingItem.setOnAction(new CrossingsHandler());
        exportHistoryItem.setOnAction(new ExportHistory());


        CheckMenuItem redrawCheckItem = new CheckMenuItem("Redraw");
        MenuItem generalSettingsItem = new MenuItem("General Settins");
        MenuItem geneSettingsItem = new MenuItem("Gene Settings");
        MenuItem importSettingsItem = new MenuItem("Import Settings");
        MenuItem exportSettingsItem = new MenuItem("Export Settings");
        settingsMenu.getItems().addAll(redrawCheckItem, generalSettingsItem, geneSettingsItem,
                importSettingsItem, exportSettingsItem);

        redrawCheckItem.setSelected(Settings.redraw);
        redrawCheckItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Settings.redraw = redrawCheckItem.isSelected();
            }
        });

        Stage generalSettingsStage = new Stage();
        //Stage geneSettingsStage = new Stage();
        generalSettingsItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                generalSettingsStage.showAndWait();
            }
        });
        geneSettingsItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pickGene.setValue("None");
                geneSettingsStage.showAndWait();
            }
        });
        importSettingsItem.setOnAction(new importSettingsHandler());
        exportHistoryItem.setOnAction(new exportSettingsHandler());


        generalSettingsStage.initStyle(StageStyle.UTILITY);
        generalSettingsStage.initModality(Modality.APPLICATION_MODAL);
        GridPane paneGeneral = new GridPane();
        paneGeneral.setHgap(10);
        paneGeneral.setVgap(5);
        paneGeneral.setAlignment(Pos.CENTER);
        Scene sceneGeneral = new Scene(paneGeneral, 340, 420);
        ColumnConstraints columnGeneral1 = new ColumnConstraints();
        columnGeneral1.setHalignment(HPos.RIGHT);
        paneGeneral.getColumnConstraints().add(columnGeneral1);
        ColumnConstraints columnGeneral2 = new ColumnConstraints();
        columnGeneral2.setHalignment(HPos.LEFT);
        paneGeneral.getColumnConstraints().add(columnGeneral2);
        generalSettingsStage.setScene(sceneGeneral);

        Label timeDiffL = new Label("Time diff");
        final TextField timeDiffF = new TextField(Double.toString(Settings.time_diff));
        paneGeneral.add(timeDiffL, 0, 0);
        paneGeneral.add(timeDiffF, 1, 0);
        timeDiffF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                if (validDouble(timeDiffF.getText())) {
                    Settings.time_diff = Double.parseDouble(timeDiffF.getText());
                    updated();
                }
            }

        });

        Label widthL = new Label("Width");
        final TextField widthF = new TextField(Integer.toString(Settings.width));
        paneGeneral.add(widthL, 0, 1);
        paneGeneral.add(widthF, 1, 1);
        widthF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = widthF.getText();
                if (validInteger(s)) {
                    Settings.width = Integer.parseInt(s);
                    cnv.setWidth(Settings.width);
                    canvas.setWidth(Settings.width);
                    primaryStage.setWidth(Settings.width);
                    stromRedraw(canvas);
                }
            }
        });

        Label heightL = new Label("Height");
        final TextField heightF = new TextField(Integer.toString(Settings.height));
        paneGeneral.add(heightL, 0, 2);
        paneGeneral.add(heightF, 1, 2);
        heightF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = heightF.getText();
                if (validInteger(s)) {
                    Settings.height = Integer.parseInt(s);
                    cnv.setHeight(Settings.height);
                    canvas.setHeight(Settings.height);
                    primaryStage.setHeight(Settings.height);
                    stromRedraw(canvas);
                }
            }
        });

        Label nodeGapL = new Label("Node Gap");
        final TextField nodeGapF = new TextField(Integer.toString(Settings.node_gap));
        paneGeneral.add(nodeGapL, 0, 3);
        paneGeneral.add(nodeGapF, 1, 3);
        nodeGapF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Settings.node_gap = Integer.parseInt(nodeGapF.getText());
                updated();
            }
        });

        Label chromosomeGapL = new Label("Chromosome Gap");
        final TextField chromosomeGapF = new TextField(Integer.toString(Settings.chromosome_gap));
        paneGeneral.add(chromosomeGapL, 0, 4);
        paneGeneral.add(chromosomeGapF, 1, 4);
        chromosomeGapF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Settings.chromosome_gap = Integer.parseInt(chromosomeGapF.getText());
                updated();
            }
        });

        Label lineGapL = new Label("Line Gap");
        final TextField lineGapF = new TextField(Integer.toString(Settings.line_gap));
        paneGeneral.add(lineGapL, 0, 5);
        paneGeneral.add(lineGapF, 1, 5);
        lineGapF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                if (validInteger(lineGapF.getText())) {
                    Settings.line_gap = Integer.parseInt(lineGapF.getText());
                    updated();
                }
            }
        });

        Label lineSizeL = new Label("Line Size");
        final TextField lineSizeF = new TextField(Integer.toString(Settings.line_size));
        paneGeneral.add(lineSizeL, 0, 6);
        paneGeneral.add(lineSizeF, 1, 6);
        lineSizeF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = lineSizeF.getText();
                if (validInteger(s)) {
                    Settings.line_size = Integer.parseInt(s);
                    updated();
                }
            }
        });

        Label satL = new Label("Saturation");
        final TextField satF = new TextField(Double.toString(Settings.saturation));
        paneGeneral.add(satL, 0, 7);
        paneGeneral.add(satF, 1, 7);
        satF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = satF.getText();
                if (validDouble(s)) {
                    Settings.saturation = Double.parseDouble(s);
                    Settings.calcColors();
                    updated();
                }
            }
        });

        Label briL = new Label("Brightness");
        final TextField briF = new TextField(Double.toString(Settings.brightness));
        paneGeneral.add(briL, 0, 8);
        paneGeneral.add(briF, 1, 8);
        briF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = briF.getText();
                if (validDouble(s)) {
                    Settings.brightness = Double.parseDouble(s);
                    Settings.calcColors();
                    updated();
                }
            }
        });

        Label optL = new Label("Optimization");
        final TextField optF = new TextField(Integer.toString(Settings.optimized));
        paneGeneral.add(optL, 0, 9);
        paneGeneral.add(optF, 1, 9);
        optF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                String s = optF.getText();
                Settings.optimized = Integer.parseInt(s);
                strom.optimize();
                updated();
            }

        });

        Label nonhighlightedL = new Label("Non highlighted");
        ColorPicker nonhighlightedP = new ColorPicker((Color) Settings.nonhighlighted);
        paneGeneral.add(nonhighlightedL, 0, 10);
        paneGeneral.add(nonhighlightedP, 1, 10);
        nonhighlightedP.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Settings.nonhighlighted = nonhighlightedP.getValue();
                updated();
            }
        });


        geneSettingsStage.initStyle(StageStyle.UTILITY);
        geneSettingsStage.initModality(Modality.APPLICATION_MODAL);
        GridPane paneGene = new GridPane();
        paneGene.setHgap(10);
        paneGene.setVgap(5);
        paneGene.setAlignment(Pos.CENTER);
        Scene sceneGene = new Scene(paneGene, 360, 310);
        ColumnConstraints columnGene1 = new ColumnConstraints();
        columnGene1.setHalignment(HPos.RIGHT);
        paneGene.getColumnConstraints().add(columnGene1);
        ColumnConstraints columnGene2 = new ColumnConstraints();
        columnGene2.setHalignment(HPos.LEFT);
        paneGene.getColumnConstraints().add(columnGene2);
        geneSettingsStage.setScene(sceneGene);

        //pickGene = new ComboBox();
        CheckBox click = new CheckBox("Select by clicking");
        click.setSelected(Settings.selectable_genes);
        click.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                drawfact.clear();
                Settings.selectable_genes = click.isSelected();
                if (click.isSelected()){
                    drawfact = new FXSelectableDrawFactory(group, geneSettingsStage, pickGene);
                } else {
                    drawfact = new FXDrawFactory(gc);
                }
                stromRedraw(canvas);
            }
        });
        paneGene.add(pickGene, 0, 0);
        paneGene.add(click, 1, 0);



        Label drawL = new Label("Draw");
        drawC = new ComboBox();
        paneGene.add(drawL, 0, 1);
        paneGene.add(drawC, 1, 1);


        Label highlightedL = new Label("Highlighted");
        highlightedC = new ComboBox();
        paneGene.add(highlightedL, 0, 2);
        paneGene.add(highlightedC, 1, 2);


        Label transparentL = new Label("Transparent");
        transparentC = new ComboBox();
        paneGene.add(transparentL, 0, 3);
        paneGene.add(transparentC, 1, 3);


        Label lineWL = new Label("Line Width");
        lineWF = new TextField(Integer.toString(Settings.line_size));
        paneGene.add(lineWL, 0, 4);
        paneGene.add(lineWF, 1, 4);


        Label nameL = new Label("Name");
        nameF = new TextField();
        paneGene.add(nameL, 0, 5);
        paneGene.add(nameF, 1, 5);


        Label colourL = new Label("Colour");
        colorP = new ColorPicker();
        paneGene.add(colourL, 0, 6);
        paneGene.add(colorP, 1, 6);


        removeMeta = new Button("Delete gene meta");

        useDefault = new Button("Delete all meta");

        paneGene.add(removeMeta, 0, 7);
        paneGene.add(useDefault, 1, 7);
        useDefault.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Settings.clearGeneMeta();
                metaclear();
                updated();
            }
        });
        removeMeta.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                int gene = (int) pickGene.getValue();
                Settings.removeMeta(gene);
                updatedgene(gene);
                updated();
            }
        });
        metaclear();
        attach_listeners();

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                Settings.width = newSceneWidth.intValue();
                cnv.setWidth(Settings.width);
                canvas.setWidth(Settings.width);
                stromRedraw(canvas);
                widthF.setText(Integer.toString(Settings.width));
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                Settings.height = newSceneHeight.intValue();
                cnv.setHeight(Settings.height);
                heightF.setText(Integer.toString(Settings.height));
            }
        });


    }

    private void stromRedraw(Canvas canvas) {
        if (!strom.isEmpty()) {
            strom.calcScale();
            strom.calcRealHeight();
            canvas.setHeight(Settings.real_height);
            draw();
        }
    }

    private static void metaclear() {
        dettach_listeners();
        pickGene.getItems().clear();
        pickGene.getItems().add("None");
        pickGene.getItems().addAll("Default");
        pickGene.getItems().addAll(strom.getGenes());
        pickGene.setValue("None");
        update_specific_panel();
        attach_listeners();
    }

    private static void updated() {
        if (Settings.redraw) {
            draw();
        }
        update_specific_panel();
    }

    private static void attach_listeners() {
        nameF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                int gene = (int) pickGene.getValue();
                Settings.set_gene_name(gene, nameF.getText());
                updatedgene(gene);
                updated();
            }

        });
        colorP.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int gene = (int) pickGene.getValue();
                Settings.set_gene_col(gene, colorP.getValue());
                updatedgene(gene);
                updated();
            }
        });
        lineWF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                int gene = (int) pickGene.getValue();
                Settings.set_gene_width(gene, Integer.parseInt(lineWF.getText()));
                updatedgene(gene);
                updated();
            }

        });
        transparentC.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Boolean val = Boolean.parseBoolean(transparentC.getValue().toString());
                if (pickGene.getValue() == "Default") {
                    Settings.default_transparent = val;
                } else {
                    int gene = (int) pickGene.getValue();
                    Settings.set_gene_trans(gene, val);
                    updatedgene(gene);

                }
                updated();
            }
        });
        highlightedC.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Boolean val = Boolean.parseBoolean(highlightedC.getValue().toString());
                if (pickGene.getValue() == "Default") {
                    Settings.default_highlighted = val;
                } else {
                    int gene = (int) pickGene.getValue();
                    Settings.set_gene_highlighted(gene, val);
                    updatedgene(gene);
                }
                updated();
            }
        });
        drawC.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Boolean val = Boolean.parseBoolean(drawC.getValue().toString());
                if (pickGene.getValue() == "Default") {
                    Settings.default_draw = val;
                } else {
                    int gene = (int) pickGene.getValue();
                    Settings.set_gene_draw(gene, val);
                    updatedgene(gene);

                }
                updated();
            }
        });
        pickGene.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                update_specific_panel();

            }
        });
    }

    private static void update_specific_panel() {
        dettach_listeners();
        if (pickGene.getValue() == "None") {
            removeMeta.setDisable(true);
            colorP.setDisable(true);
            nameF.setDisable(true);
            lineWF.setDisable(true);
            transparentC.setDisable(true);
            highlightedC.setDisable(true);
            drawC.setDisable(true);
        } else {
            if (pickGene.getValue() == "Default") {
                colorP.setDisable(true);
                nameF.setDisable(true);
                lineWF.setDisable(true);
                transparentC.setDisable(false);
                highlightedC.setDisable(false);
                drawC.setDisable(false);

                transparentC.getItems().clear();
                transparentC.getItems().add("True");
                transparentC.getItems().add("False");
                transparentC.setValue("False");
                if (Settings.default_transparent) {
                    transparentC.setValue("True");
                }
                highlightedC.getItems().clear();
                highlightedC.getItems().add("True");
                highlightedC.getItems().add("False");
                highlightedC.setValue("False");
                if (Settings.default_highlighted) {
                    highlightedC.setValue("True");
                }

                drawC.getItems().clear();
                drawC.getItems().add("True");
                drawC.getItems().add("False");
                drawC.setValue("False");
                if (Settings.default_draw) {
                    drawC.setValue("True");
                }

            } else {
                int gene = Integer.parseInt(pickGene.getValue().toString());
                updatedgene(gene);
            }
        }
        attach_listeners();
    }

    private static void dettach_listeners() {
        nameF.setOnAction(null);
        colorP.setOnAction(null);
        lineWF.setOnAction(null);
        transparentC.setOnAction(null);
        highlightedC.setOnAction(null);
        drawC.setOnAction(null);
        pickGene.setOnAction(null);

    }

    private static void updatedgene(int gene) {
        dettach_listeners();
        removeMeta.setDisable(false);
        colorP.setDisable(false);
        nameF.setDisable(false);
        lineWF.setDisable(false);
        transparentC.setDisable(false);
        highlightedC.setDisable(false);
        drawC.setDisable(false);

        transparentC.getItems().clear();
        transparentC.getItems().add("True");
        transparentC.getItems().add("False");


        highlightedC.getItems().clear();
        highlightedC.getItems().add("True");
        highlightedC.getItems().add("False");


        drawC.getItems().clear();
        drawC.getItems().add("True");
        drawC.getItems().add("False");
        nameF.clear();
        lineWF.clear();
        if (!Settings.gene_meta.containsKey(gene)) {
            transparentC.getItems().add(0, "<Default>");
            highlightedC.getItems().add(0, "<Default>");
            drawC.getItems().add(0, "<Default>");
            transparentC.setValue("<Default>");
            highlightedC.setValue("<Default>");
            drawC.setValue("<Default>");
            nameF.clear();
            lineWF.clear();
        } else {
            if (Settings.is_draw(gene)) {
                drawC.setValue("True");
            } else {
                drawC.setValue("False");
            }
            if (Settings.isHighlighted(gene)) {
                highlightedC.setValue("True");
            } else {
                highlightedC.setValue("False");
            }
            if (Settings.isTransparent(gene)) {
                transparentC.setValue("True");
            } else {
                transparentC.setValue("False");
            }
            String s = Settings.gene_name(gene);
            if (s != null) {
                nameF.setText(s);
            }
            Integer lw = Settings.gene_meta.get(gene).getLine_width();
            if (lw != null) {
                lineWF.setText(lw.toString());
            }

        }
        colorP.setValue((Color) Settings.gene_color(gene));
        attach_listeners();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        for (String s : args) {
            String[] splitted = s.split(":");
            switch (splitted[0]) {
                case "-load_settings":
                    load_settings = splitted[1];
                    break;
                case "-exportgreedy":
                    export_greedy = true;
                    break;
                case "-exportilp":
                    export_ilp = true;
                    break;
                case "-exportminimized":
                    export_minimized = true;
                case "-exportcrossings":
                    export_crossings = true;
                    break;
                case "-drawsvg":
                    draw_svg = true;
                    break;
                case "-input":
                    input = splitted[1];
                    break;
                case "-nogui":
                    gui = false;
                    break;
                case "-opt":
                    optimize = true;
                    Settings.optimized = Integer.parseInt(splitted[1]);
                    break;
                case "-greedy_output":
                    greedy_output = splitted[1];
                    break;
                case "-ilp_output":
                    ilp_output = splitted[1];
                    break;
                case "-svg_output":
                    svg_output = splitted[1];
                    break;
                case "-load_lp":
                    load_lp = splitted[1];
                    break;
                case "-minimized_output":
                    minimized_output = splitted[1];
                    break;
                case "-crossings_output":
                    crossings_output = splitted[1];
                    break;
            }
        }
        if (gui) {
            launch(args);
        } else {
            File f = new File(input);
            strom.load(f);
            if (optimize) {
                strom.findBlocks();
            }
            if (export_greedy) {
                File file = new File(greedy_output);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                strom.exportBlocks(bw);
                fw.close();

            }
            if (export_ilp) {
                File file = new File(ilp_output);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                strom.exportILP(bw);
                fw.close();
            }
            if (export_minimized) {
                File file = new File(minimized_output);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                strom = strom.level_by_level_sweep();
                strom.exportCrossings(bw);
                fw.close();
                export_minimized = false;
            }
            if (export_crossings) {
                File file = new File(crossings_output);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(strom.countCrossings(strom.getRoot()).toString());
                bw.close();
                fw.close();
                export_crossings = false;
            }
            if (load_settings != null) {
                Settings.loadXML(load_settings);
            }
            if (load_lp != null) {
                File lp = new File(load_lp);
                strom.loadILP(lp);
            }
            if (draw_svg) {
                DrawFactory drawF = new SVGDrawFactory();
                drawF.clear();
                strom.print(drawF);
                File file = new File(svg_output);
                file.delete();
                file.createNewFile();
                drawF.export(file);
            }
            //System.exit(0);
        }
    }

    private static void draw() {
        if (!strom.isEmpty()) {
            //DrawFactory drawF = new FXSelectableDrawFactory(group, geneSettingsStage, pickGene);
            drawfact.clear();
            strom.print(drawfact);
//            EvolutionTree.EvolutionNode next = strom.getRoot();
//            while(next != null){
//                for (int i = 0; i<next.chromosomes.size(); i++){
//                    System.out.println("chromosome "+i+" genepos: " + next.chromosomes.get(i).genePos);
//                    //System.out.println(strom.which_chromosome(next, i));
//                }
//
//                next = next.getFirst();
//                System.out.println();
//            }
        }
    }

    private static boolean validDouble(String s) {
        return true;
    }

    private static boolean validInteger(String s) {
        return true;
    }

    private static class OpenHandler implements EventHandler<ActionEvent> {

        private final Stage primaryStage;
        private Canvas canvas;

        public OpenHandler(Stage _primaryStage, Canvas canvas) {
            this.primaryStage = _primaryStage;
            this.canvas = canvas;
        }

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("History file (*.history)", "*.history");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                Settings.title = file.getName();
                primaryStage.setTitle(Settings.title);
                try {
                    strom = new EvolutionTree();
                    strom.load(file);
                    strom.calcRealHeight();
                    canvas.setHeight(Settings.real_height);
                    draw();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
                }
                metaclear();
            }
        }


    }

    private static class SaveImgHandler implements EventHandler<ActionEvent> {

        public SaveImgHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            /**
             * DrawFactory drawF = new SVGDrawFactory(); strom.print(drawF);
             * drawF.export();
             *
             */
            FileChooser fileChooser1 = new FileChooser();
            fileChooser1.setTitle("Save Image");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SVG files (*.svg)", "*.svg");
            fileChooser1.getExtensionFilters().add(extFilter);
            File file = fileChooser1.showSaveDialog(primaryStage);

            DrawFactory drawF = new SVGDrawFactory();
            strom.print(drawF);
            drawF.export(file);
        }
    }

    private static class redrawHandler implements EventHandler<ActionEvent> {

        public redrawHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            draw();
        }
    }

    private static class importSettingsHandler implements EventHandler<ActionEvent> {

        public importSettingsHandler() {
        }

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("xml files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                Settings.loadXML(file);
                updated();
            }
        }
    }

    private static class exportSettingsHandler implements EventHandler<ActionEvent> {

        public exportSettingsHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser1 = new FileChooser();
            fileChooser1.setTitle("Export Settings");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("xml files (*.xml)", "*.xml");
            fileChooser1.getExtensionFilters().add(extFilter);
            File file = fileChooser1.showSaveDialog(primaryStage);
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
            Settings.exportXML(file);

        }
    }

    private static class OptimizeHandler implements EventHandler<ActionEvent> {

        public OptimizeHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            strom.optimize();
            updated();
        }
    }

    private static class exportILPHandler implements EventHandler<ActionEvent> {

        public exportILPHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            try {
                FileChooser fileChooser1 = new FileChooser();
                fileChooser1.setTitle("Export ILP");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ILP files (*.lp)", "*.lp");
                fileChooser1.getExtensionFilters().add(extFilter);
                File file = fileChooser1.showSaveDialog(primaryStage);
                file.delete();
                file.createNewFile();
                FileWriter fw = null;
                try {
                    fw = new FileWriter(file);
                } catch (IOException ex) {
                    Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
                }
                BufferedWriter bw = new BufferedWriter(fw);
                try {
                    strom.exportILP(bw);
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class importILPHandler implements EventHandler<ActionEvent> {

        public importILPHandler() {
        }

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Solution files (*.sol)", "*.sol");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    strom.loadILP(file);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
                }
                updated();
            }
        }
    }

    private static class CrossingsHandler implements EventHandler<ActionEvent> {

        public CrossingsHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            strom = strom.level_by_level_sweep();
            draw();
        }

    }

    private static class ExportHistory implements EventHandler<ActionEvent> {

        public ExportHistory() {

        }

        @Override
        public void handle(ActionEvent event) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export history");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HISTORY files (*.history)", "*.history");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(primaryStage);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                strom.exportCrossings(bw);
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
