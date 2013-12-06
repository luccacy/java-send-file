import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class Zip {  
    /** 
     * 默认构造函数 
     */  
    public Zip(){  
          
    }  
  
    /** 
     * @desc 将源文件/文件夹生成指定格式的压缩文件,格式zip 
     * @param resourePath 源文件/文件夹 
     * @param targetPath  目的压缩文件保存路径 
     * @return void 
     * @throws Exception  
     */  
    public void compressedFile(String resourcesPath,String targetPath) throws Exception{  
        File resourcesFile = new File(resourcesPath);     //源文件  
        File targetFile = new File(targetPath);           //目的  
        
          
        FileOutputStream outputStream = new FileOutputStream(targetPath);  
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));  
          
        createCompressedFile(out, resourcesFile, "");  
          
        out.close();    
    }  
      
    /** 
     * @desc 生成压缩文件。 
     *               如果是文件夹，则使用递归，进行文件遍历、压缩 
     *       如果是文件，直接压缩 
     * @param out  输出流 
     * @param file  目标文件 
     * @return void 
     * @throws Exception  
     */  
    public void createCompressedFile(ZipOutputStream out,File file,String dir) throws Exception{  
        //如果当前的是文件夹，则进行进一步处理  
        if(file.isDirectory()){  
            //得到文件列表信息  
            File[] files = file.listFiles();  
            //将文件夹添加到下一级打包目录  
            out.putNextEntry(new ZipEntry(dir+"/"));  
              
            dir = dir.length() == 0 ? "" : dir +"/";  
              
            //循环将文件夹中的文件打包  
            for(int i = 0 ; i < files.length ; i++){  
                createCompressedFile(out, files[i], dir + files[i].getName());         //递归处理  
            }  
        }  
        else{   //当前的是文件，打包处理  
            //文件输入流  
            FileInputStream fis = new FileInputStream(file);  
              
            out.putNextEntry(new ZipEntry(dir));  
            //进行写操作  
            int j =  0;  
            byte[] buffer = new byte[1024];  
            while((j = fis.read(buffer)) > 0){  
                out.write(buffer,0,j);  
            }  
            //关闭输入流  
            fis.close();  
        }  
    }  
    
    public static void unZipFiles(String zipPath,String descDir)throws IOException{  
        unZipFiles(new File(zipPath), descDir);  
    }  
    
    public static void unZipFiles(File zipFile,String descDir)throws IOException{  
        File pathFile = new File(descDir);  
        if(!pathFile.exists()){  
            pathFile.mkdirs();  
        }  
        ZipFile zip = new ZipFile(zipFile);  
        for(Enumeration entries = zip.entries();entries.hasMoreElements();){  
            ZipEntry entry = (ZipEntry)entries.nextElement();  
            String zipEntryName = entry.getName();  
            InputStream in = zip.getInputStream(entry);  
            String outPath = (descDir+zipEntryName).replaceAll("\\*", "/");;  
            //判断路径是否存在,不存在则创建文件路径  
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));  
            if(!file.exists()){  
                file.mkdirs();  
            }  
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压  
            if(new File(outPath).isDirectory()){  
                continue;  
            }  
            //输出文件路径信息  
            System.out.println(outPath);  
              
            OutputStream out = new FileOutputStream(outPath);  
            byte[] buf1 = new byte[1024];  
            int len;  
            while((len=in.read(buf1))>0){  
                out.write(buf1,0,len);  
            }  
            in.close();  
            out.close();  
            }  
        System.out.println("******************解压完毕********************");  
    }  
      
//    public static void main(String[] args){  
//    	Zip compressedFileUtil = new Zip();  
//          
//        try {  
//            //compressedFileUtil.compressedFile("e:\\update4.1", "e:\\update4.1.zip");  
//            compressedFileUtil.unZipFiles("e:/update4.1.zip", "d:/update4.1");
//            System.out.println("压缩文件已经生成...");  
//        } catch (Exception e) {  
//            System.out.println("压缩文件生成失败...");  
//            e.printStackTrace();  
//        }  
//    }  
}  
