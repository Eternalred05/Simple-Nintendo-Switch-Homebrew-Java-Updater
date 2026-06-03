package GUI;

import Logic.AppsManagement;
import Logic.GitHubService;
import Logic.NxApp;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.kohsuke.github.GHAsset;

public class SwitchMenu extends javax.swing.JFrame {

    ArrayList<NxApp> apps = new ArrayList<>();
    private GitHubService gitHubService;
    private int pendingVersionTasks = 0;
    private boolean isLoadingVersions = false;

    public SwitchMenu() {
        initComponents();
        // welcomeMessage();
        setLocationRelativeTo(null);
        setTitle("Nintendo Homebrew Java Updater v1.3.3");
        setResizable(false);
        configureTable();
        try {
            gitHubService = new GitHubService();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error while connecting to Github: " + e.getMessage(),
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private int countTotalAssets(ArrayList<NxApp> appsList) {
        int total = 0;
        for (NxApp app : appsList) {
            List<GHAsset> assets = gitHubService.getReleaseAssets(app.getRepoOwner(), app.getRepoName());
            if (assets != null) {
                total += assets.size();
            }
        }
        return total;
    }

    private void downloadFile(String fileURL, String outputPath) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");
        httpConn.setConnectTimeout(30000);
        httpConn.setReadTimeout(60000);
        httpConn.setInstanceFollowRedirects(true);

        Path outputFile = Paths.get(outputPath);
        Path parentDir = outputFile.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            System.out.println("Folder Created: " + parentDir);
        }

        try (InputStream inputStream = httpConn.getInputStream()) {
            Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File Created: " + outputFile.toAbsolutePath());
        } finally {
            httpConn.disconnect();
        }
    }

    private void updateProgressAssets(int current, int total) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            progressBar.setValue(current);
            progressBar.setString(current + "/" + total);
        });
    }

    private void downloadAsset(GHAsset asset, String downloadDir) throws IOException {
        String fileName = asset.getName();
        String downloadUrl = asset.getBrowserDownloadUrl();
        String outputPath = downloadDir + File.separator + fileName;
        downloadFile(downloadUrl, outputPath);
        System.out.println("Asset downloaded: " + fileName);
    }

    private void welcomeMessage() {
        JOptionPane.showMessageDialog(null, "This Application was developed by EternalRed05, any sugerences can be made via github issues. Thanks for using!.", "Welcome", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addElements() {
        DefaultTableModel model = (DefaultTableModel) appTable.getModel();
        model.setRowCount(0);
        for (NxApp n : apps) {
            Object[] o = new Object[]{n.getName(), n.getRepoOwner(), n.getUrl(), n.getVersion()};
            model.addRow(o);
        }

    }

    private void configureTable() {
        String[] columnas = {"App Name", "Developer", "Source", "Latest Version"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appTable.setModel(model);
        appTable.getTableHeader().setReorderingAllowed(false);
        appTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        appTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        appTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        appTable.getColumnModel().getColumn(3).setPreferredWidth(100);

    }

    private void updateVersionsFromGitHub(Runnable onComplete) {

        if (isLoadingVersions) {
            System.out.println("Already loading versions, ignoring request.");
            return;
        }
        if (apps.isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        isLoadingVersions = true;
        pendingVersionTasks = apps.size();

        downloadAll.setEnabled(false);
        jButton1.setEnabled(false);
        if (reloadButton != null) {
            reloadButton.setEnabled(false);
        }

        switchButton.setEnabled(false);
        n3dsButton.setEnabled(false);
        Wii.setEnabled(false);
        WiiU.setEnabled(false);
        vWii.setEnabled(false);

        lblStatus.setText("Loading latest versions...");
        progressBar.setIndeterminate(true);

        DefaultTableModel model = (DefaultTableModel) appTable.getModel();

        for (int i = 0; i < apps.size(); i++) {
            final int row = i;
            final NxApp app = apps.get(i);

            new SwingWorker<Void, Void>() {
                private String latestVersion;

                @Override
                protected Void doInBackground() {
                    latestVersion = gitHubService.getLatestVersion(app.getRepoOwner(), app.getRepoName());
                    return null;
                }

                @Override
                protected void done() {

                    if (latestVersion != null && !latestVersion.equals("Error")) {
                        app.setVersion(latestVersion);
                        model.setValueAt(latestVersion, row, 3);
                    } else {
                        model.setValueAt("Unknown", row, 3);
                    }

                    pendingVersionTasks--;
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        if (pendingVersionTasks > 0) {
                            lblStatus.setText("Loading versions... (" + pendingVersionTasks + " remaining)");
                        }
                    });

                    if (pendingVersionTasks == 0) {
                        isLoadingVersions = false;
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                }
            }.execute();
        }
    }

    private void onVersionsLoaded() {
        javax.swing.SwingUtilities.invokeLater(() -> {

            downloadAll.setEnabled(true);
            jButton1.setEnabled(true);
            if (reloadButton != null) {
                reloadButton.setEnabled(true);
            }
            switchButton.setEnabled(true);
            n3dsButton.setEnabled(true);
            Wii.setEnabled(true);
            WiiU.setEnabled(true);
            vWii.setEnabled(true);

            lblStatus.setText("Ready");
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);

            JOptionPane.showMessageDialog(SwitchMenu.this,
                    "All versions have been updated.",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        appTable = new javax.swing.JTable();
        downloadAll = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        lblStatus = new javax.swing.JLabel();
        switchButton = new javax.swing.JButton();
        n3dsButton = new javax.swing.JButton();
        Wii = new javax.swing.JButton();
        WiiU = new javax.swing.JButton();
        reloadButton = new javax.swing.JButton();
        vWii = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("Download Selected");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        appTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(appTable);

        downloadAll.setText("Download All Apps");
        downloadAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadAllActionPerformed(evt);
            }
        });

        lblStatus.setText("Actual Status");

        switchButton.setText("Nintendo Switch");
        switchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchButtonActionPerformed(evt);
            }
        });

        n3dsButton.setText("Nintendo 3DS");
        n3dsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                n3dsButtonActionPerformed(evt);
            }
        });

        Wii.setText("Wii");
        Wii.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WiiActionPerformed(evt);
            }
        });

        WiiU.setText("Wii U");
        WiiU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WiiUActionPerformed(evt);
            }
        });

        reloadButton.setText("Reload Latest Versions");
        reloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadButtonActionPerformed(evt);
            }
        });

        vWii.setText("vWii");
        vWii.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vWiiActionPerformed(evt);
            }
        });

        jButton2.setText("About");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(switchButton)
                        .addGap(24, 24, 24)
                        .addComponent(n3dsButton))
                    .addComponent(downloadAll, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(204, 204, 204)
                        .addComponent(lblStatus))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(Wii)
                        .addGap(18, 18, 18)
                        .addComponent(WiiU)
                        .addGap(20, 20, 20)
                        .addComponent(vWii)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 255, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reloadButton))
                .addGap(53, 53, 53))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 525, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(210, 210, 210))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addContainerGap())))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(41, 41, 41)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 952, Short.MAX_VALUE)
                    .addGap(41, 41, 41)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(n3dsButton)
                    .addComponent(Wii)
                    .addComponent(switchButton)
                    .addComponent(WiiU)
                    .addComponent(reloadButton)
                    .addComponent(vWii))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 431, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(downloadAll, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblStatus, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(102, 102, 102)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(102, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void downloadAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadAllActionPerformed
        downloadAll.setEnabled(false);
        jButton1.setEnabled(false);
        switchButton.setEnabled(false);
        n3dsButton.setEnabled(false);
        Wii.setEnabled(false);
        WiiU.setEnabled(false);
        vWii.setEnabled(false);
        downloadAll.setText("Downloading...");
        lblStatus.setText("Please wait until all files are processed...");
        progressBar.setIndeterminate(true);

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return countTotalAssets(apps);
            }

            @Override
            protected void done() {
                try {
                    int totalAssets = get();
                    progressBar.setIndeterminate(false);
                    progressBar.setMaximum(totalAssets);
                    progressBar.setValue(0);
                    progressBar.setStringPainted(true);
                    if (totalAssets == 0) {
                        JOptionPane.showMessageDialog(SwitchMenu.this, "There are no files to download.", "Warning", JOptionPane.WARNING_MESSAGE);
                        restoreButtons();
                        return;
                    }

                    startDownloadAll(totalAssets);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(SwitchMenu.this, "Error while counting files to download: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    restoreButtons();
                }
            }
        }.execute();

    }//GEN-LAST:event_downloadAllActionPerformed
    private void restoreButtons() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            downloadAll.setEnabled(true);
            jButton1.setEnabled(true);
            downloadAll.setText("Download All Apps");
            jButton1.setText("Download Selected");
            lblStatus.setText("Ready");
            progressBar.setValue(0);
            progressBar.setString("");
        });
    }

    private void startDownloadAll(int totalAssets) {
        new Thread(() -> {
            int processedAssets = 0;
            int successApps = 0;
            StringBuilder errors = new StringBuilder();

            for (int i = 0; i < apps.size(); i++) {
                NxApp app = apps.get(i);
                final int appIndex = i + 1;
                javax.swing.SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Processing " + app.getName() + " (" + appIndex + "/" + apps.size() + ")");
                });

                String version = app.getVersion();
                if (version == null || version.equals("Unknown") || version.equals("Error")) {
                    errors.append("• ").append(app.getName()).append(": Version not available\n");
                    continue;
                }

                try {
                    List<GHAsset> assets = gitHubService.getReleaseAssets(app.getRepoOwner(), app.getRepoName());
                    if (assets == null || assets.isEmpty()) {
                        errors.append("• ").append(app.getName()).append(": No assets in release\n");
                        continue;
                    }

                    String appFolder = System.getProperty("user.home") + "/Downloads/NxAppDownloader/"
                            + app.getName().replaceAll("\\s+", "_") + " " + app.getVersion().replaceAll("\\s+", "_");
                    java.nio.file.Files.createDirectories(java.nio.file.Paths.get(appFolder));

                    for (GHAsset asset : assets) {
                        try {
                            downloadAsset(asset, appFolder);
                            processedAssets++;

                            final int currentProcessed = processedAssets;
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                progressBar.setValue(currentProcessed);
                                progressBar.setString(currentProcessed + "/" + totalAssets);
                            });
                        } catch (IOException e) {
                            errors.append("• ").append(app.getName()).append(" - ").append(asset.getName()).append(": ").append(e.getMessage()).append("\n");
                        }
                    }
                    successApps++;
                } catch (Exception e) {
                    errors.append("• ").append(app.getName()).append(": ").append(e.getMessage()).append("\n");
                    e.printStackTrace();
                }
            }

            final int finalSuccessApps = successApps;
            final int finalProcessedAssets = processedAssets;
            final String finalErrors = errors.toString();
            javax.swing.SwingUtilities.invokeLater(() -> {
                String message = String.format("Download completed.\nSuccessful Apps: %d/%d\nDownloaded Files: %d/%d",
                        finalSuccessApps, apps.size(), finalProcessedAssets, totalAssets);
                if (finalErrors.length() > 0) {
                    message += "\n\nErrors:\n" + finalErrors;
                }
                JOptionPane.showMessageDialog(SwitchMenu.this, message, "Result",
                        (finalSuccessApps == apps.size()) ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                restoreButtons();
            });
        }).start();
    }

    private void updateProgressSelected(int current, int total) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            progressBar.setValue(current);
            progressBar.setString(current + "/" + total);
        });
    }

    private void updateProgress(int value) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(value + "/" + apps.size());
        });
    }
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int[] selectedRows = appTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one application.",
                    "No selection",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            ArrayList<NxApp> selectedApps = new ArrayList<>();
            for (int row : selectedRows) {
                selectedApps.add(apps.get(row));
            }

            downloadAll.setEnabled(false);
            jButton1.setEnabled(false);
            switchButton.setEnabled(false);
            n3dsButton.setEnabled(false);
            Wii.setEnabled(false);
            WiiU.setEnabled(false);
            vWii.setEnabled(false);
            jButton1.setText("Downloading selected...");
            lblStatus.setText("Please wait until all files are processed...");
            progressBar.setIndeterminate(true);

            new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() {
                    return countTotalAssets(selectedApps);
                }

                @Override
                protected void done() {
                    try {
                        int totalAssets = get();
                        progressBar.setIndeterminate(false);
                        progressBar.setMaximum(totalAssets);
                        progressBar.setValue(0);
                        progressBar.setStringPainted(true);
                        if (totalAssets == 0) {
                            JOptionPane.showMessageDialog(SwitchMenu.this,
                                    "No assets found for the selected applications.",
                                    "Warning",
                                    JOptionPane.WARNING_MESSAGE);
                            restoreButtons();
                            return;
                        }
                        startDownloadSelected(selectedApps, totalAssets);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(SwitchMenu.this,
                                "Error counting assets: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        restoreButtons();
                    }
                }
            }.execute();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void switchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchButtonActionPerformed
        apps = AppsManagement.addNXApps();
        addElements();
        updateVersionsFromGitHub(this::onVersionsLoaded);

    }//GEN-LAST:event_switchButtonActionPerformed

    private void n3dsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_n3dsButtonActionPerformed
        apps = AppsManagement.addCTRApps();
        addElements();
        updateVersionsFromGitHub(this::onVersionsLoaded);
    }//GEN-LAST:event_n3dsButtonActionPerformed

    private void WiiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WiiActionPerformed
        apps = AppsManagement.addDolphinApps();
        addElements();
        updateVersionsFromGitHub(this::onVersionsLoaded);
    }//GEN-LAST:event_WiiActionPerformed

    private void WiiUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WiiUActionPerformed
        apps = AppsManagement.addCafeApps();
        addElements();
        updateVersionsFromGitHub(this::onVersionsLoaded);
    }//GEN-LAST:event_WiiUActionPerformed

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadButtonActionPerformed
        if (isLoadingVersions) {
            JOptionPane.showMessageDialog(this, "Already loading versions, please wait.", "Busy", JOptionPane.INFORMATION_MESSAGE);

        } else {
            updateVersionsFromGitHub(() -> {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    downloadAll.setEnabled(true);
                    jButton1.setEnabled(true);
                    switchButton.setEnabled(true);
                    n3dsButton.setEnabled(true);
                    Wii.setEnabled(true);
                    WiiU.setEnabled(true);
                    vWii.setEnabled(true);
                    reloadButton.setEnabled(true);
                    lblStatus.setText("Ready");
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    JOptionPane.showMessageDialog(this, "Versions reloaded successfully.", "Done", JOptionPane.INFORMATION_MESSAGE);
                });
            });
        }
    }//GEN-LAST:event_reloadButtonActionPerformed

    private void vWiiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vWiiActionPerformed
        apps = AppsManagement.addvWiiApps();
        addElements();
        updateVersionsFromGitHub(this::onVersionsLoaded);
    }//GEN-LAST:event_vWiiActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        JOptionPane.showMessageDialog(null, "Small App Developed by EternalRed05, any sugerence can be made at https://github.com/Eternalred05/Simple-Nintendo-Consoles-Homebrew-Java-Updater ", "Information", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void startDownloadSelected(java.util.ArrayList<NxApp> selectedApps, int totalAssets) {
        new Thread(() -> {
            int processedAssets = 0;
            int successApps = 0;
            StringBuilder errors = new StringBuilder();

            for (int i = 0; i < selectedApps.size(); i++) {
                NxApp app = selectedApps.get(i);
                final int appIndex = i + 1;
                javax.swing.SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Processing " + app.getName() + " (" + appIndex + "/" + selectedApps.size() + ")");
                });

                String version = app.getVersion();
                if (version == null || version.equals("Unknown") || version.equals("Error")) {
                    errors.append("• ").append(app.getName()).append(": Version not available\n");
                    continue;
                }

                try {
                    java.util.List<org.kohsuke.github.GHAsset> assets = gitHubService.getReleaseAssets(app.getRepoOwner(), app.getRepoName());
                    if (assets == null || assets.isEmpty()) {
                        errors.append("• ").append(app.getName()).append(": No assets in release\n");
                        continue;
                    }

                    String appFolder = System.getProperty("user.home") + "/Downloads/NxAppDownloader/"
                            + app.getName().replaceAll("\\s+", "_") + " " + app.getVersion().replaceAll("\\s+", "_");
                    java.nio.file.Files.createDirectories(java.nio.file.Paths.get(appFolder));

                    for (org.kohsuke.github.GHAsset asset : assets) {
                        try {
                            downloadAsset(asset, appFolder);
                            processedAssets++;
                            final int currentProcessed = processedAssets;
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                progressBar.setValue(currentProcessed);
                                progressBar.setString(currentProcessed + "/" + totalAssets);
                            });
                        } catch (IOException e) {
                            errors.append("• ").append(app.getName()).append(" - ").append(asset.getName()).append(": ").append(e.getMessage()).append("\n");
                        }
                    }
                    successApps++;
                } catch (Exception e) {
                    errors.append("• ").append(app.getName()).append(": ").append(e.getMessage()).append("\n");
                    e.printStackTrace();
                }
            }

            final int finalSuccessApps = successApps;
            final int finalProcessedAssets = processedAssets;
            final String finalErrors = errors.toString();
            javax.swing.SwingUtilities.invokeLater(() -> {
                String message = String.format("Download completed.\nSuccessful apps: %d/%d\nDownloaded files: %d/%d",
                        finalSuccessApps, selectedApps.size(), finalProcessedAssets, totalAssets);
                if (finalErrors.length() > 0) {
                    message += "\n\nErrors:\n" + finalErrors;
                }
                JOptionPane.showMessageDialog(SwitchMenu.this, message, "Result (Selected)",
                        (finalSuccessApps == selectedApps.size()) ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                restoreButtons();
            });
        }).start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SwitchMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SwitchMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SwitchMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SwitchMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SwitchMenu().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Wii;
    private javax.swing.JButton WiiU;
    private javax.swing.JTable appTable;
    private javax.swing.JButton downloadAll;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JButton n3dsButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton reloadButton;
    private javax.swing.JButton switchButton;
    private javax.swing.JButton vWii;
    // End of variables declaration//GEN-END:variables
}
