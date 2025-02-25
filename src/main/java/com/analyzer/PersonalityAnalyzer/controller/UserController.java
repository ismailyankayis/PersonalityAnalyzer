package com.analyzer.PersonalityAnalyzer.controller;

import com.analyzer.PersonalityAnalyzer.ZemberekConnection;
import com.analyzer.PersonalityAnalyzer.entity.Result;
import com.analyzer.PersonalityAnalyzer.entity.StringResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import com.analyzer.PersonalityAnalyzer.entity.User;
import com.analyzer.PersonalityAnalyzer.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/User")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    ResultController resultController;

    ZemberekConnection zemberekCon = new ZemberekConnection();
    Logger LOGGER = Logger.getLogger(UserController.class.getName());
    private String predictCommand = "cmd /c python src/main/LIWC/predictPersonality.py ";

    @RequestMapping(path="/findAll", method = RequestMethod.GET)
    public  @ResponseBody
    List<User> findAll(){
        return userService.findAll();
    }

    @RequestMapping(path="/findUserById/{id}", method = RequestMethod.GET)
    public  @ResponseBody User findUserById(@PathVariable String id){
        return userService.findUserById(id);
    }

    @RequestMapping(path="/findUserByUsername/{username}", method = RequestMethod.GET)
    public @ResponseBody User findUserByUsername(@PathVariable String username){
        return userService.findUserByUsername(username);
    }

    @RequestMapping(path="/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody
    HttpStatus update (@RequestBody User usr){
        userService.update(usr);
        return HttpStatus.OK;
    }

    @RequestMapping(path="/analyzeButton/{username}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public StringResponse analyzeButton(@PathVariable String username){
        User usr = null;
        String returnMessage = zemberekCon.getTweets(username);
        String result_id = "";
        StringResponse sr = new StringResponse(returnMessage);

        if(returnMessage.equals("Başarılı")) {
            usr = userService.findUserByUsername(username);
            List<Object> returnValues = zemberekCon.normalizeTweets(usr);
            List<String> tweets = (List<String>)returnValues.get(0);
            usr.setPreprocessedTweets(tweets);
            update(usr);
            zemberekCon.findWordgroups(usr.getUsername());
            result_id = /*callPrediction(username);*/"5ce69a2d90e1ad23040b4d12";
            Result result = resultController.findResultById(result_id);
            resultController.update(result);
            sr.setData(result_id);
        }
        return sr;
    }

    private String callPrediction(String username) {
        LOGGER.info("Prediction function started for " + username);
        String result_id = "";
        try {
            String line;
            Process p = Runtime.getRuntime().exec(predictCommand + username);
            BufferedReader bri = new BufferedReader
                    (new InputStreamReader(p.getInputStream()));
            while ((line = bri.readLine()) != null) {
                if(line.contains("result_id:"))
                    result_id = line.split(":")[1];
                System.out.println(line);
            }
            bri.close();
            p.waitFor();
            LOGGER.info("Prediction function finished for " + username);
        } catch (Exception err) {
            err.printStackTrace();
        }
        return result_id;
    }
}