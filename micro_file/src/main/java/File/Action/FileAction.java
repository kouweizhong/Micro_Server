package File.Action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import File.Main.Config;

@RestController
public class FileAction {

	@Autowired
	public Config config;

	@RequestMapping(value = "/picture", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> uploadFile(MultipartHttpServletRequest request) {
		File fileUploadPath = new File(config.getTmpInfoFilePath());
		if (!fileUploadPath.exists())
			fileUploadPath.mkdirs();
		List<MultipartFile> files = request.getFiles("file");
		String realFileName = null;
		if (!files.get(0).isEmpty()) {
			String originalFileName = files.get(0).getOriginalFilename();
			UUID uuid = UUID.randomUUID();
			realFileName = uuid + originalFileName.substring(originalFileName.lastIndexOf("."));
			try {
				byte[] bytes = files.get(0).getBytes();
				FileOutputStream fos = new FileOutputStream(
						config.getTmpInfoFilePath() + File.separator + realFileName);// 写入文件
				fos.write(bytes);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				// throw new IFException("读取文件出错！"+e.getStackTrace());
			}
		}
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("fileName", realFileName);
		return resultMap;
	}

	/**
	 * 下载临时文件图片 根据imgUri获得图片并输出到response
	 * 
	 * @param request
	 * @param response
	 * @param fileName
	 * @param fileSuffix
	 */
	@RequestMapping(value = "/tmpPicture/{fileName}/{fileSuffix}", method = RequestMethod.GET)
	public @ResponseBody void downloadTmpFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String fileName, @PathVariable String fileSuffix) {
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		String filePath = config.getTmpInfoFilePath() + "/" + fileName + "." + fileSuffix;
		try {
			FileInputStream imageIn = new FileInputStream(filePath);
			BufferedInputStream bis = new BufferedInputStream(imageIn);
			BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
			byte data[] = new byte[4096];
			int size = 0;
			size = bis.read(data);
			while (size != -1) {
				bos.write(data, 0, size);
				size = bis.read(data);
			}
			bis.close();
			bos.flush();
			bos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 下载图片 根据imgUri获得图片并输出到response
	 * 
	 * @param request
	 * @param response
	 * @param fileName
	 * @param fileSuffix
	 */
	@RequestMapping(value = "/Picture/{fileName}/{fileSuffix}", method = RequestMethod.GET)
	public @ResponseBody void downloadFile(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String fileName, @PathVariable String fileSuffix) {

		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		String filePath = config.getRealInfofilePath() + "/" + fileName + "." + fileSuffix;
		try {
			FileInputStream imageIn = new FileInputStream(filePath);
			BufferedInputStream bis = new BufferedInputStream(imageIn);
			BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
			byte data[] = new byte[4096];
			int size = 0;
			size = bis.read(data);
			while (size != -1) {
				bos.write(data, 0, size);
				size = bis.read(data);
			}
			bis.close();
			bos.flush();
			bos.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 *
	 * 将图片从临时目录移动到真实目录，并删除临时路径图片
	 * @param fileNames 移动图片的名称数组 例如 [a.png,b.jpb,c.png]
	 */

	@RequestMapping(value = "/Copy/{fileNames}", method = RequestMethod.POST)
	public @ResponseBody void Copy(@PathVariable String[] fileNames) {
		File file = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		for (String fileName : fileNames) {
			try {
				file = new File(config.getTmpInfoFilePath() + "/" + fileName);
				if (!file.exists()) {
					continue;
				}
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				fos = new FileOutputStream(config.getRealInfofilePath() + "/" + fileName);// 写入文件
				byte data[] = new byte[4096];
				int size = 0;
				size = bis.read(data);
				while (size != -1) {
					fos.write(data, 0, size);
					size = bis.read(data);
				}
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
				// throw new IFException("500","将图片从临时路径拷贝到真实路径出错...");
			} finally {
				try {
					if (fis != null)
						fis.close();
					if (bis != null)
						bis.close();
					if (fos != null)
						fos.close();
					if (file.exists())
						file.delete();// 删除临时路径图片
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

}
