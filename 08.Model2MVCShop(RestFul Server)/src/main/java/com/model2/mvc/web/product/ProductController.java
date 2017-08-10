package com.model2.mvc.web.product;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.FileSystemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.common.util.CommonUtil;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.Reply;
import com.model2.mvc.service.product.ProductService;

@Controller
@RequestMapping("product/*")
public class ProductController {

	/*Field*/
	@Autowired
	@Qualifier("productService")
	private ProductService productService;
	
	@Value("#{commonProperties['pageUnit'] ?: 5}")
	int pageUnit;
	
	@Value("#{commonProperties['pageSize'] ?: 3}")
	int pageSize;

	@Autowired
	@Qualifier("uploadFilePath")
	private FileSystemResource fsr;
	
	String temDir =
			"C:\\Users\\ljw12\\git\\07.Model2MVCShop\\07.Model2MVCShop(URI,pattern)\\WebContent\\images\\uploadFiles";
			//request.getServletContext().getRealPath("images\\uploadFiles");

	
	/*Constructor*/
	public ProductController(){
		System.out.println(getClass());
	}
	
	/*Method*/
	@RequestMapping( value="addProduct", method=RequestMethod.GET )
	public ModelAndView addProduct() throws Exception{
		return new ModelAndView("forward:addProductView.jsp");
	}
	
	@RequestMapping( value="addProduct", method=RequestMethod.POST )
	public ModelAndView addProduct( @ModelAttribute("product") Product product,
									@RequestParam("file") MultipartFile file		) throws Exception{
		
		ModelAndView modelAndView = new ModelAndView("forward:addProduct.jsp");


//////////FileUpload 추가////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		if(FileUpload.isMultipartContent(request)){
//			String temDir = 
//					"C:\\Users\\ljw12\\git\\07.Model2MVCShop\\07.Model2MVCShop(URI,pattern)\\WebContent\\images\\uploadFiles";
//			
//			DiskFileUpload fileUpload = new DiskFileUpload();
//			fileUpload.setRepositoryPath(temDir);
//			fileUpload.setSizeMax(1024*1024*1);
//			fileUpload.setSizeThreshold(1024*100);
//			
//			if(request.getContentLength() < fileUpload.getSizeMax()){
//				Product product = new Product();
//				StringTokenizer token = null;
//				
//				List<FileItem> fileItemList = fileUpload.parseRequest(request);
//				for(FileItem fileItem : fileItemList){
//					if(fileItem.isFormField()){
//						if(fileItem.getFieldName().equals("manuDate")){
//							product.setManuDate(fileItem.getString("euc-kr"));
//						} else if(fileItem.getFieldName().equals("prodName")){
//							product.setProdName(fileItem.getString("euc-kr"));
//						} else if(fileItem.getFieldName().equals("prodDetail")){
//							product.setProdDetail(fileItem.getString("euc-kr"));
//						} else if(fileItem.getFieldName().equals("price")){
//							product.setPrice(Integer.parseInt(fileItem.getString("euc-kr")));
//						} else if(fileItem.getFieldName().equals("stock")){
//							product.setStock(Integer.parseInt(fileItem.getString("euc-kr")));
//						}
//					}else{
//						if(fileItem.getSize()>0){
//							int idx = fileItem.getName().lastIndexOf("\\");
//							if(idx == -1){
//								idx = fileItem.getName().lastIndexOf("/");
//							}
//							String fileName = fileItem.getName().substring(idx+1);
//							product.setFileName(fileName);
//							try{
//								File uploadedFile = new File(temDir, fileName);
//								fileItem.write(uploadedFile);
//							}catch(IOException e){
//								System.out.println(e);
//							}
//						}else{
//							product.setFileName("empty"+(int)(Math.random()*3)+".GIF");
//						}
//					}
//				}
//				productService.addProduct(product);
//				modelAndView.addObject("product", product);
//			} else{
//				int overSize = (request.getContentLength()/1000000);
//				System.out.println("<script>alert('파일의 크기는 1MB까지 입니다. 올리신 파일 용량은 "+overSize + "MB 입니다.');");
//				System.out.println("history.back();</script>");
//			}
//		}else{
//			System.out.println("인코딩 타입이 multipart/form-data가 아닙니다..");
//		}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Spring MultipartFile 이용
//		
//		MultipartFile file = ((MultipartHttpServletRequest)request).getFile("file");
//		
		product.setFileName("");
		if(!file.isEmpty()){
			FileOutputStream fos = new FileOutputStream(new File(fsr.getPath(), file.getOriginalFilename()));
			fos.write(file.getBytes());
			fos.flush();
			fos.close();
			product.setFileName(file.getOriginalFilename());
		}
		
		productService.addProduct(product);
		
		return modelAndView;
	}
	
	@RequestMapping( value="getProduct", method=RequestMethod.GET )
	public ModelAndView getProduct(	@RequestParam("menu") String menu,
									@RequestParam("prodNo") int prodNo,
									@CookieValue(value="history", required=false) String history, 
									HttpServletResponse response	) throws Exception{
		
		Product product = productService.getProduct(prodNo);
		product.setReplyList(productService.getProductCommentList(prodNo));
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("product", product);
		modelAndView.addObject("replyList", product.getReplyList());
		
		String viewName = "redirect:updateProduct?prodNo="+prodNo;
		
		if(menu.equals("search")){
			String newHistory = prodNo + "";

			if(history != null){
				for(String h : history.split(",")){
					if(!CommonUtil.null2str(h).equals(new Integer(prodNo).toString())){
						newHistory += "," + h;
					}
				}
			}
			history = newHistory;
			
			Cookie cookie = new Cookie("history",history);
			cookie.setPath("/");
			response.addCookie(cookie);
			
			viewName = "forward:getProduct.jsp";
		}
		
		modelAndView.setViewName(viewName);
		
		return modelAndView;
	}
	
	@RequestMapping( value="updateProduct", method=RequestMethod.POST )
	public ModelAndView updateProduct(	@ModelAttribute("product") Product product,
										@RequestParam("file") MultipartFile file	) throws Exception{


		if(!file.isEmpty()){
			file.transferTo(new File(fsr.getPath(), file.getOriginalFilename()));
			product.setFileName(file.getOriginalFilename());
		}
		
		productService.updateProduct(product);
		product=productService.getProduct(product.getProdNo());
		
		return new ModelAndView("forward:getProduct.jsp?menu=manage&prodNo="+product.getProdNo(), "product", product);
	}
	
	@RequestMapping( value="updateProduct", method=RequestMethod.GET )
	public ModelAndView updateProduct( @RequestParam("prodNo") int prodNo) throws Exception{
		Product product = productService.getProduct(prodNo);
		
		return new ModelAndView("forward:updateProductView.jsp", "product", product);
	}
	
	
	
	@RequestMapping( value="listProduct" )
	public ModelAndView listProduct(@ModelAttribute("search") Search search, @RequestParam("menu") String menu) throws Exception{
		if(search.getCurrentPage()==0){
			search.setCurrentPage(1);
		}
		if(menu.equals("manage")){
			search.setStockView(true);
		}
		search.setPageSize(pageSize);
		search.setPageUnit(pageUnit);
		
		if(search.getSearchCondition() != null && search.getSearchCondition().equals("2")){
			try{
				Integer.parseInt(search.getSearchKeyword());
			}catch(NumberFormatException e){
				search.setSearchKeyword("");
			}
			try{
				Integer.parseInt(search.getSearchKeyword2());
			}catch(NumberFormatException e){
				search.setSearchKeyword2("");
			}
		}
		
		Map<String, Object> map = productService.getProductList(search);
		
		Page resultPage = new Page(search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("forward:listProduct.jsp?menu="+menu);
		modelAndView.addObject("list", map.get("list"));
		modelAndView.addObject("resultPage", resultPage);
		
		return modelAndView;
	}
	
	@RequestMapping( value="addProductComment", method=RequestMethod.POST )
	public ModelAndView addProductComment(@ModelAttribute("product") Product product, @ModelAttribute("reply") Reply reply) throws Exception{
		List<Reply> list = new ArrayList<Reply>();
		
		list.add(reply);
		product.setReplyList(list);
		
		productService.addProductComment(product);
		
		return new ModelAndView("redirect:getProduct?menu=search&prodNo="+product.getProdNo());
	}
	
}
