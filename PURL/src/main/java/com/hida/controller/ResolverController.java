package com.hida.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.hida.model.Purl;
import com.hida.service.ResolverService;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * A controller class that paths the user to all jsp files in WEB_INF/jsp.
 *
 * @author leland lopez
 * @author lruffin
 */
@Controller
@RequestMapping("/")
public class ResolverController {

    /* 
     * Logger; logfile to be stored in resource folder    
     */
    private static final org.slf4j.Logger Logger
            = LoggerFactory.getLogger(ResolverController.class);

    @Autowired
    private ResolverService ResolverService;

    /**
     * Maps to the home page.
     *
     * @return view to the home page
     */
    @RequestMapping(value = {""}, method = {RequestMethod.GET})
    public ModelAndView displayIndex() {
        ModelAndView model = new ModelAndView();
        model.setViewName("home");

        return model;
    }

    /**
     * matches url: /PURL/retrieve retrieves corresponding purl row of provided
     * purlid returns model - purl and view : retrieve if successful returns
     * model - null if not
     *
     * @param purlid purlid of desired retrieved row
     * @return ModelAndView Holds resulting Model and view information
     * @throws IOException Thrown by Jackson library
     */
    @RequestMapping("/retrieve")
    public ModelAndView retrieve(@RequestParam(value = "purlid", required = true) String purlid)
            throws IOException {
        if (Logger.isInfoEnabled()) {
            Logger.info("Retrieve was Called");
        }
        // retrieve purl jsonString
        Purl purl = ResolverService.retrieveModel(purlid);

        // show retrieve view, attach purl jsonString.  converted to json at view.
        String jsonString = this.convertPurlToJson(purl);
        ModelAndView mv = new ModelAndView("result", "message", jsonString);

        Logger.info("Retrieve returned: " + null);
        return mv;
    }

    /**
     * matches url: /PURL/edit edit purlid row url, with provided url returns
     * model : purl and view : edit if successful returns model : null if not
     *
     * @param purlid purlid of desired edited row
     * @param url The url that the desired purl will have
     * @return ModelAndView Holds resulting Model and view information
     * @throws IOException Thrown by Jackson library
     */
    @RequestMapping("/edit")
    public ModelAndView edit(@RequestParam(value = "purlid", required = true) String purlid,
            @RequestParam(value = "url", required = true) String url) throws IOException {
        if (Logger.isInfoEnabled()) {
            Logger.info("Edit was Called");
        }
        // edit the purl and then retrieve its entire contents
        ResolverService.editURL(purlid, url);
        Purl purl = ResolverService.retrieveModel(purlid);

        // show edit view, attach purl jsonString.  converted to json at view.
        String jsonString = this.convertPurlToJson(purl);
        ModelAndView mv = new ModelAndView("result", "message", jsonString);

        Logger.info("Edit returned: " + jsonString);
        return mv;

    }

    /**
     * matches url: /PURL/insert inserts purlid, url, erc, who, what, when to
     * new row of table returns model : purl and view : insert if successful
     * returns model : null if not
     *
     * @param purlid purlid to be inserted
     * @param url url to be inserted
     * @param erc erc to be inserted
     * @param who who to be inserted
     * @param what what to be inserted
     * @param when when to be insertd
     * @return ModelAndView Holds resulting Model and view information
     * @throws IOException Thrown by Jackson library
     */
    @RequestMapping("/insert")
    public ModelAndView insert(@RequestParam(value = "purlid", required = true) String purlid,
            @RequestParam(value = "url", required = true) String url,
            @RequestParam(value = "erc", required = true) String erc,
            @RequestParam(value = "who", required = true) String who,
            @RequestParam(value = "what", required = true) String what,
            @RequestParam(value = "when", required = true) String when
    ) throws IOException {
        if (Logger.isInfoEnabled()) {
            Logger.info("Insert was Called");
        }
        // create purl jsonString to store information
        Purl purl = new Purl(purlid, url, erc, who, what, when);

        // insert purl
        ResolverService.insertPURL(purl);

        //show edit view, attach purl jsonString.  converted to json at view.
        String jsonString = this.convertPurlToJson(purl);
        ModelAndView mv = new ModelAndView("result", "message", jsonString);

        Logger.info("insert returned: " + null);
        return mv;

    }

    /**
     * matches url: /PURL/delete deletes row of table with corresponding purlid
     * returns view : deleted if successful returns model : null if not
     *
     * @param purlid purlid of desired deleted row
     * @return ModelAndView Holds resulting Model and view information
     * @throws IOException Thrown by Jackson library
     */
    @RequestMapping("/delete")
    public ModelAndView delete(@RequestParam(value = "purlid", required = true) String purlid)
            throws IOException {
        if (Logger.isInfoEnabled()) {
            Logger.info("Insert was Called");
        }
        // create json jsonString that designates success
        final String resultJson = "{\"result\":\"deleted\"}";

        // delete purl
        ResolverService.deletePURL(purlid);

        //show edit view, attach purl jsonString.  converted to json at view.
        ModelAndView mv = new ModelAndView("result", "message", resultJson);
        Logger.info("insert returned: " + resultJson);

        mv.addObject("message", resultJson);
        return mv;
    }

    /**
     * Throws any exception that may be caught within the program
     *
     * @param req the HTTP request
     * @param exception the caught exception
     * @return The view of the error message
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralError(HttpServletRequest req, Exception exception) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("status", 500);
        mav.addObject("exception", exception.getClass().getSimpleName());
        mav.addObject("message", exception.getMessage());
        Logger.error("General Error: " + exception.getMessage());

        StackTraceElement[] trace = exception.getStackTrace();
        String error = "";
        for (StackTraceElement element : trace) {
            error += element.toString() + "\n";
        }

        mav.addObject("stacktrace", error);

        mav.setViewName("error");
        return mav;
    }

    /**
     * Creates a Json jsonString based off a set of purl given in the parameter
     *
     * @param purl Entity to convert the jsonString into
     * @return A reference to a String that contains Json set of ids
     * @throws IOException Thrown by Jackson's IO framework
     */
    private String convertPurlToJson(Purl purl) throws IOException {

        // Jackson objects to format JSON strings
        String jsonString;
        ObjectMapper mapper = new ObjectMapper();
        Object formattedJson;

        // create json jsonString
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("pid", purl.getIdentifier())
                .add("url", purl.getURL())
                .add("erc", purl.getERC())
                .add("who", purl.getWho())
                .add("what", purl.getWhat())
                .add("date", purl.getDate())
                .build();

        // format json array
        formattedJson = mapper.readValue(jsonObject.toString(), Object.class);
        jsonString = mapper.writerWithDefaultPrettyPrinter().
                writeValueAsString(formattedJson);

        return jsonString;
    }
}
