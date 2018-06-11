/*
The following are top voted examples for showing how to use org.apache.hadoop.fs.FSDataOutputStream.
These examples are extracted from open source projects.
You can vote up the examples you like and your votes will be used in our system to product more good examples.
*/

// Example 1

private void writeConfigFile(FileSystem fs, Path name, ArrayList<String> nodes) throws IOException {
  // delete if it already exists
  if (fs.exists(name)) {
    fs.delete(name, true);
  }

  FSDataOutputStream stm = fs.create(name);

  if (nodes != null) {
    for (Iterator<String> it = nodes.iterator(); it.hasNext();) {
      String node = it.next();
      stm.writeBytes(node);
      stm.writeBytes("\n");
    }
  }
  stm.close();
}


// Example 2

@Override
public void run() {
  FSDataOutputStream out = null;
  int i = 0;
  try {
    out = fs.create(filepath);
    for(; running; i++) {
      System.out.println(getName() + " writes " + i);
      out.write(i); out.hflush(); sleep(100);
    }
  } catch(Exception e) {
    System.out.println(getName() + " dies: e=" + e);
  } finally {
    System.out.println(getName() + ": i=" + i);
    IOUtils.closeStream(out);
  }
}


// Example 3

/**
* @param compressionAlgo
* The compression algorithm to be used to for compression.
*/
public WBlockState(Algorithm compressionAlgo, FSDataOutputStream fsOut, BytesWritable fsOutputBuffer, Configuration conf) throws IOException {
  this.compressAlgo = compressionAlgo;
  this.fsOut = fsOut;
  this.posStart = fsOut.getPos();
  fsOutputBuffer.setCapacity(TFile.getFSOutputBufferSize(conf));
  this.fsBufferedOutput = new SimpleBufferedOutputStream(this.fsOut, fsOutputBuffer.get());
  this.compressor = compressAlgo.getCompressor();
  try {
    this.out = compressionAlgo.createCompressionStream(fsBufferedOutput, compressor, 0);
  } catch(IOException e) {
    compressAlgo.returnCompressor(compressor);
    throw e;
  }
}

// Example 4

public void run() {
  FSDataOutputStream out = null; int i = 0;
  try {
    out = fs.create(filepath);
    for(; running; i++) {
      System.out.println(getName() + " writes " + i);
      out.write(i);
      out.sync();
      sleep(100);
    }
  } catch(Exception e) {
    System.out.println(getName() + " dies: e=" + e);
  } finally {
    System.out.println(getName() + ": i=" + i);
    IOUtils.closeStream(out);
  }
}

// Example 5

public void testBadIndex() throws Exception {
  final int parts = 30;
  JobConf conf = new JobConf();
  FileSystem fs = FileSystem.getLocal(conf).getRaw();
  Path p = new Path(System.getProperty("test.build.data", "/tmp"), "cache").makeQualified(fs);
  fs.delete(p, true); conf.setInt("mapred.tasktracker.indexcache.mb", 1);
  IndexCache cache = new IndexCache(conf);
  Path f = new Path(p, "badindex");
  FSDataOutputStream out = fs.create(f, false);
  CheckedOutputStream iout = new CheckedOutputStream(out, new CRC32());
  DataOutputStream dout = new DataOutputStream(iout);
  for (int i = 0; i < parts; ++i) {
    for (int j = 0; j < MapTask.MAP_OUTPUT_INDEX_RECORD_LENGTH / 8; ++j) {
      if (0 == (i % 3)) {
        dout.writeLong(i);
      } else {
        out.writeLong(i);
      }
    }
  }
  out.writeLong(iout.getChecksum().getValue());
  dout.close();
  try {
    cache.getIndexInformation("badindex", 7, f);
    fail("Did not detect bad checksum");
  } catch (IOException e) {
    if (!(e.getCause() instanceof ChecksumException)) {
      throw e;
    }
  }
}

// Example 6

/**
* Tests the fileLength when we sync the file and restart the cluster and
* Datanodes not report to Namenode yet.
*/
@Test(timeout = 60000)
public void testFileLengthWithHSyncAndClusterRestartWithOutDNsRegister() throws Exception {
  final Configuration conf = new HdfsConfiguration(); // create cluster conf.setInt(DFSConfigKeys.DFS_BLOCK_SIZE_KEY, 512);
  final MiniDFSCluster cluster = new MiniDFSCluster.Builder(conf) .numDataNodes(2).build();
  HdfsDataInputStream in = null;
  try {
    Path path = new Path("/tmp/TestFileLengthOnClusterRestart", "test");
    DistributedFileSystem dfs = (DistributedFileSystem) cluster.getFileSystem();
    FSDataOutputStream out = dfs.create(path);
    int fileLength = 1030;
    out.write(new byte[fileLength]);
    out.hsync();
    cluster.restartNameNode();
    cluster.waitActive();
    in = (HdfsDataInputStream) dfs.open(path, 1024);
    // Verify the length when we just restart NN. DNs will register // immediately.
    Assert.assertEquals(fileLength, in.getVisibleLength()); cluster.shutdownDataNodes();
    cluster.restartNameNode(false);
    // This is just for ensuring NN started. verifyNNIsInSafeMode(dfs);
      try {
        in = (HdfsDataInputStream) dfs.open(path);
        Assert.fail("Expected IOException");
      } catch (IOException e) {
        Assert.assertTrue(e.getLocalizedMessage().indexOf("Name node is in safe mode") >= 0);
      }
    } finally {
      if (null != in) {
        in.close();
      }
      cluster.shutdown();
    }
  }


// Example 8

/**
* Persists a job in DFS.
*
* @param job the job about to be 'retired' */
public void store(JobInProgress job) {
  if (active && retainTime > 0) {
    JobID jobId = job.getStatus().getJobID();
    Path jobStatusFile = getInfoFilePath(jobId);
    try {
      FSDataOutputStream dataOut = fs.create(jobStatusFile);
      job.getStatus().write(dataOut);
      job.getProfile().write(dataOut);
      job.getCounters().write(dataOut);
      TaskCompletionEvent[] events = job.getTaskCompletionEvents(0, Integer.MAX_VALUE);
      dataOut.writeInt(events.length);
      for (TaskCompletionEvent event : events) {
        event.write(dataOut);
      }
      dataOut.close();
    } catch (IOException ex) {
      LOG.warn("Could not store [" + jobId + "] job info : " + ex.getMessage(), ex);
      try {
        fs.delete(jobStatusFile, true);
      } catch (IOException ex1) {
        //ignore
      }
    }
  }
}

// Example 9

public void run() {
  System.out.println("Workload starting ");
  for (int i = 0; i < numberOfFiles; i++) {
    Path filename = new Path(id + "." + i);
    try {
      System.out.println("Workload processing file " + filename);
      FSDataOutputStream stm = createFile(fs, filename, replication);
      DFSClient.DFSOutputStream dfstream = (DFSClient.DFSOutputStream) (stm.getWrappedStream());
      dfstream.setArtificialSlowdown(1000);
      writeFile(stm, myseed);
      stm.close();
      checkFile(fs, filename, replication, numBlocks, fileSize, myseed);
    } catch (Throwable e) {
      System.out.println("Workload exception " + e);
      assertTrue(e.toString(), false);
    }
    // increment the stamp to indicate that another file is done.
    synchronized (this) {
      stamp++;
    }
  }
}

// Example 10

/**
* Test when input files are from non-default file systems */
@Test
public void testForNonDefaultFileSystem() throws Throwable {
  Configuration conf = new Configuration();
  // use a fake file system scheme as default
  conf.set(CommonConfigurationKeys.FS_DEFAULT_NAME_KEY, DUMMY_FS_URI);
  // default fs path
  assertEquals(DUMMY_FS_URI, FileSystem.getDefaultUri(conf).toString());
  // add a local file
  Path localPath = new Path("testFile1");
  FileSystem lfs = FileSystem.getLocal(conf);
  FSDataOutputStream dos = lfs.create(localPath);
  dos.writeChars("Local file for CFIF");
  dos.close();
  Job job = Job.getInstance(conf);
  FileInputFormat.setInputPaths(job, lfs.makeQualified(localPath));
  DummyInputFormat inFormat = new DummyInputFormat();
  List<InputSplit> splits = inFormat.getSplits(job);
  assertTrue(splits.size() > 0);
  for (InputSplit s : splits) {
    CombineFileSplit cfs = (CombineFileSplit)s;
    for (Path p : cfs.getPaths()) {
      assertEquals(p.toUri().getScheme(), "file");
    }
  }
}


// Example 11

public void testOutputStreamClosedTwice() throws IOException {
  //HADOOP-4760 according to Closeable#close() closing already-closed
  //streams should have no effect.
  Path src = path("/test/hadoop/file");
  FSDataOutputStream out = fs.create(src);
  out.writeChar('H'); //write some data out.close();
  out.close();
}

// Example 12


@SuppressWarnings("unchecked")
private static <T extends InputSplit> SplitMetaInfo[] writeNewSplits(Configuration conf,
T[] array, FSDataOutputStream out) throws IOException, InterruptedException {
  SplitMetaInfo[] info = new SplitMetaInfo[array.length];
  if (array.length != 0) {
    SerializationFactory factory = new SerializationFactory(conf);
    int i = 0;
    long offset = out.size();
    for(T split: array) {
      int prevCount = out.size();
      Text.writeString(out, split.getClass().getName());
      Serializer<T> serializer = factory.getSerializer((Class<T>) split.getClass());
      serializer.open(out);
      serializer.serialize(split);
      int currCount = out.size();
      String[] locations = split.getLocations();
      final int max_loc = conf.getInt(MAX_SPLIT_LOCATIONS, 10);
      if (locations.length > max_loc) {
        LOG.warn("Max block location exceeded for split: " + split + " splitsize: " + locations.length + " maxsize: " + max_loc);
        locations = Arrays.copyOf(locations, max_loc);
      }
      info[i++] = new JobSplit.SplitMetaInfo(locations, offset, split.getLength());
      offset += currCount - prevCount;
    }
  }
  return info;
}

// Example 13

public void processUpload(HttpServletRequest request,HttpServletResponse response) throws Exception{
  ServletFileUpload upload = new ServletFileUpload();
  upload.setProgressListener(new UploadListener());
  //Parse the request
  FileItemIterator iter = upload.getItemIterator(request);
  String owner = null;//= request.getParameter("uname");
  String category = null;//= request.getParameter("category");
  Map<String,String> map = new HashMap<String,String> (10);

  upload.setHeaderEncoding("utf-8");

  //Process the uploaded items
  int uploadCunt = 0;
  //Image[] infos = null;
  //Exif[] exifs = null;
  String albumImage = null;
  long usedSpace = 0L;
  while (iter.hasNext()) {
    FileItemStream item = iter.next();
    InputStream stream = item.openStream();
    if(item.isFormField()) {
      String fieldName = item.getFieldName();
      String value = Streams.asString(stream);
      map.put(fieldName,value);
    } else {
      owner = map.get("uname");
      category = map.get("category");
      if(owner == null || category == null) {
        continue;
      }

      byte[] bytes= new byte[BUFFER_SIZE];
      int inLength = 0;
      inLength = stream.read(bytes, 0, BUFFER_SIZE);
      String fileName = item.getName();
      if (fileName != null) {
        fileName = FilenameUtils.getName(fileName);
      }
      //String fileName = new String(item.getName().getBytes("ISO8859_1"),"UTF-8"); //get input stream;
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      if(inLength < 1)
        continue;
      do{
        bo.write(bytes,0,inLength);
      } while((inLength = stream.read(bytes, 0, BUFFER_SIZE)) > 0);
      bo.flush();
      stream.close();
      //if upload item is null,continue int sizeInBytes = bo.size();
      if(sizeInBytes < 10) continue;
      String type = ImageManipulation.getImageType(bo.toByteArray());
      if(!type.equals(ImageManipulation.TYPE_JPEG)) {
        continue;
      }
      uploadCunt++;//= Integer.getInteger(map.get("num"), 0);
      //infos = new Image[uploadCunt];
      //exifs = new Exif[uploadCunt];
      Image info = new Image();
      Exif exif = null;
      info.setFileName(fileName);
      info.setCategory(category);
      info.setOwner(owner);
      info.setTimestamp(new Date(System.currentTimeMillis()));
      info.setKbytes(sizeInBytes / 1024 );
      usedSpace += (long)sizeInBytes;
      String suffix = fileName.substring(fileName.lastIndexOf('.'));
      String imagePath = (IMAGE_FILE_PATH +owner+"/"+category+"/"+KeyUtil.getAuthKey(fileName,12)).replace(' ','+')+suffix; //save file to dfs
      Path path = new Path(imagePath);
      albumImage = imagePath;
      info.setImgsrc(path.toString());
      FSDataOutputStream fout = fs.create(path, true, BUFFER_SIZE); fout.write(bo.toByteArray(),0,sizeInBytes);
      fout.flush();
      fout.close();
      //call metrics
      myMetrics.createFile(); myMetrics.wroteBytes(sizeInBytes); //get exif
      if(type == ImageManipulation.TYPE_JPEG){
        ByteArrayInputStream mbi = new ByteArrayInputStream(bo.toByteArray());
        Metadata metadata = JpegMetadataReader.readMetadata(mbi);
        Directory directory = metadata.getDirectory(ExifDirectory.class);
        exif = new Exif(directory);
        mbi.close();
      }
      //get image width and height
      ByteArrayInputStream ibi = new ByteArrayInputStream(bo.toByteArray());
      BufferedImage image = ImageManipulation.getImage(ibi);
      info.setType(type);
      info.setWidth(image.getWidth());
      info.setHeight(image.getHeight());
      ibi.close();
      bo.close();
      server.setImages(owner, new Image[]{info});
      if(exif != null) {
        server.setExif(owner, info, exif);
      }
      if(uploadCunt > 0) {
        Category album = server.getCategory(owner, category);
        album.setCount(album.getCount() + uploadCunt );
        album.setLastupload(new Date());
        if(albumImage != null && !album.isSetAlbumPhoto()){
          album.setImgurl(albumImage);
        }
        if(usedSpace > 0L) {
          album.setUsedSpace(album.getUsedSpace() + usedSpace);
        }
        server.setCategory(owner, new Category[]{album});
        if(usedSpace > 0L) {
          UserProfile[] user = server.getUser(owner,1);
          if(user != null && user.length == 1){
            user[0].setUesdeSpace(user[0].getUesdeSpace() + usedSpace);
            server.setUser(user);
          }
        }
      }
      String redir = map.get("redir"); response.sendRedirect(redir);
      if (redir != null) {
        response.setStatus(response.SC_OK);
      }
    }
}

// Example 14

public void testCopyDfsToDfsUpdateWithSkipCRC() throws Exception {
  MiniDFSCluster cluster = null;
  try {
    Configuration conf = new Configuration();
    cluster = new MiniDFSCluster(conf, 2, true, null);
    final FileSystem hdfs = cluster.getFileSystem();
    final String namenode = hdfs.getUri().toString();
    FileSystem fs = FileSystem.get(URI.create(namenode), new Configuration());
    // Create two files of the same name, same length but different
    // contents
    final String testfilename = "test";
    final String srcData = "act act act";
    final String destData = "cat cat cat";
    if (namenode.startsWith("hdfs://")) {
      deldir(hdfs,"/logs");
      Path srcPath = new Path("/srcdat", testfilename);
      Path destPath = new Path("/destdat", testfilename);
      FSDataOutputStream out = fs.create(srcPath, true);
      out.writeUTF(srcData);
      out.close();
      out = fs.create(destPath, true);
      out.writeUTF(destData);
      out.close();

      // Run with -skipcrccheck option
      ToolRunner.run(new DistCpV1(conf), new String[] {
        "-p",
        "-update",
        "-skipcrccheck",
        "-log",
        namenode+"/logs",
        namenode+"/srcdat",
        namenode+"/destdat"
      });

      // File should not be overwritten
      FSDataInputStream in = hdfs.open(destPath);
      String s = in.readUTF();
      System.out.println("Dest had: " + s);
      assertTrue("Dest got over written even with skip crc", s.equalsIgnoreCase(destData));
      in.close();

      deldir(hdfs, "/logs");

      // Run without the option
      ToolRunner.run(new DistCpV1(conf), new String[] {
        "-p",
        "-update",
        "-log",
        namenode+"/logs",
        namenode+"/srcdat",
        namenode+"/destdat"
      });

      // File should be overwritten
      in = hdfs.open(destPath);
      s = in.readUTF();
      System.out.println("Dest had: " + s);
      assertTrue("Dest did not get overwritten without skip crc", s.equalsIgnoreCase(srcData));
      in.close();

      deldir(hdfs, "/destdat"); deldir(hdfs, "/srcdat");
      deldir(hdfs, "/logs");
    }
  } finally {
    if (cluster != null) {
      cluster.shutdown();
  }
}
