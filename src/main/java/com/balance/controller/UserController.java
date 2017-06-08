package com.balance.controller;

import com.balance.model.*;
import com.balance.repository.TerminalRepository;
import com.balance.service.*;
import com.sun.prism.PhongMaterial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

@Controller
public class UserController {

    private UserService userService;
    private TerminalService terminalService;
    private BandModelService bandModelService;
    private CaloriesHistoryService caloriesHistoryService;
    private PulseHistoryService pulseHistoryService;
    private StepsHistoryService stepsHistoryService;
    private LocationHistoryService locationHistoryService;

    @Autowired
    public void setLocationHistoryService(LocationHistoryService locationHistoryService) {
        this.locationHistoryService = locationHistoryService;
    }

    @Autowired
    public void setStepsHistoryService(StepsHistoryService stepsHistoryService) {
        this.stepsHistoryService = stepsHistoryService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setTerminalService(TerminalService terminalService){
        this.terminalService=terminalService;
    }

    @Autowired
    public void setBandModelService(BandModelService bandModelService){
        this.bandModelService=bandModelService;
    }

    @Autowired
    public void setCaloriesHistoryService(CaloriesHistoryService caloriesHistoryService){
        this.caloriesHistoryService=caloriesHistoryService;
    }

    @Autowired
    public void setPulseHistoryService(PulseHistoryService pulseHistoryService) {
        this.pulseHistoryService = pulseHistoryService;
    }

    @RequestMapping(value = "/admin/user/{id}", method = RequestMethod.GET)
    public String showUser(@PathVariable Integer id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "admin/user";
    }

    @RequestMapping(value = "/admin/user/delete/{id}", method = RequestMethod.GET)
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "redirect:/admin/home";
    }

    @RequestMapping(value = "/admin/user/edit/{id}",method = RequestMethod.GET)
    public String editUser(@PathVariable Integer id,Model model) {
        model.addAttribute("user",userService.getUserById(id));
        return "admin/userForm";
    }

    @RequestMapping(value = "/admin/user", method = RequestMethod.POST)
    public String saveUser(@Valid User user) {
        userService.saveUserEdited(user);
        return "redirect:/admin/home";
    }

    @RequestMapping(value = "/admin/userHistory/pulse/{id}", method = RequestMethod.GET)
    public String viewPulseHistory(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id);
        Iterator<PulseHistory> iterator = pulseHistoryService.listAllPulseHistory().iterator();
        ArrayList<PulseHistory> resp = new ArrayList<PulseHistory>();
        while(iterator.hasNext()){
            PulseHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
                resp.add(aux);
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("pulses", resp);

        return "admin/userHistory/pulse";
    }

    @RequestMapping(value = "/admin/userHistory/calories/{id}", method = RequestMethod.GET)
    public String viewCaloriesHistory(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id);
        Iterator<CaloriesHistory> iterator2 = caloriesHistoryService.listAllCaloriesHistorys().iterator();
        ArrayList<CaloriesHistory> resp2=new ArrayList<CaloriesHistory>();
        while(iterator2.hasNext()){
            CaloriesHistory aux = iterator2.next();
            if(aux.getUser().equals(user.getId())) {
                resp2.add(aux);
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("calories",resp2);

        return "admin/userHistory/calories";
    }

    @RequestMapping(value = "admin/userHistory/steps/{id}", method = RequestMethod.GET)
    public String viewStepsHistory(@PathVariable Integer id,Model model) {
        User user = userService.getUserById(id);
        Iterator<StepsHistory> iterator2 = stepsHistoryService.listAllStepsHistory().iterator();
        ArrayList<StepsHistory> resp2=new ArrayList<StepsHistory>();
        while(iterator2.hasNext()){
            StepsHistory aux = iterator2.next();
            if(aux.getUser().equals(user.getId())) {
                resp2.add(aux);
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("steps",resp2);

        return "admin/userHistory/steps";
    }








        //LimitedUser Controller--------------------------------------------------------

    @RequestMapping(value = "/user/profile",method = RequestMethod.GET)
    public String viewProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        Terminal ant=terminalService.getTerminalById(userService.getUserById(user.getId()).getTerminal().getSerial());
        ant.setActive(true);
        terminalService.saveTerminal(ant);
        model.addAttribute("user", user);
        return "limited/profile";
    }

    @RequestMapping(value = "/user/edit",method = RequestMethod.GET)
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        model.addAttribute("user",userService.getUserById(user.getId()));

        Terminal ant=terminalService.getTerminalById(userService.getUserById(user.getId()).getTerminal().getSerial());
        ant.setActive(false);

        terminalService.saveTerminal(ant);
        model.addAttribute("bands",bandModelService.listAllBandModels());

        return "limited/editProfile";
    }

    @RequestMapping(value = "/user",method = RequestMethod.POST)
    public String saveLimitedUser(@Valid User user, BindingResult bindingResult) {
        if(user.getTerminal()==null) {
            return "redirect:/user/edit";
        }
        Terminal newTerminal = terminalService.getTerminalById(user.getTerminal().getSerial());

        if(!newTerminal.getBandModel().getName().equals(user.getBand()) || newTerminal.isActive()){
            return "redirect:/user/edit";
        }

        if(bindingResult.hasErrors()) {
            return "limited/editProfile";
        }

        newTerminal.setActive(true);
        terminalService.saveTerminal(newTerminal);
        user.setTerminal(newTerminal);

        userService.saveUserEdited(user);
        return "redirect:/user/profile/";
    }


    //Historiales
    @RequestMapping(value = "/user/CaloriesHistory", method = RequestMethod.GET)
    public String getCaloriesHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());

        Iterator<CaloriesHistory> iterator = caloriesHistoryService.listAllCaloriesHistorys().iterator();
        ArrayList<CaloriesHistory> resp=new ArrayList<CaloriesHistory>();
        while(iterator.hasNext()){
            CaloriesHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
                resp.add(aux);
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("calories",resp);
        return "limited/caloriesHistory";
    }

    @RequestMapping(value = "/user/PulseHistory", method = RequestMethod.GET)
    public String getPulsesHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        Iterator<PulseHistory> iterator = pulseHistoryService.listAllPulseHistory().iterator();
        ArrayList<PulseHistory> resp = new ArrayList<PulseHistory>();
        while(iterator.hasNext()){
            PulseHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
               resp.add(aux);
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("pulses", resp);
        return "limited/pulseHistory";
    }

    @RequestMapping(value = "/user/StepsHistory", method = RequestMethod.GET)
    public String getStepsHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        Iterator<StepsHistory> iterator = stepsHistoryService.listAllStepsHistory().iterator();
        ArrayList<StepsHistory> resp = new ArrayList<StepsHistory>();
        while(iterator.hasNext()){
            StepsHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
                resp.add(aux);
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("steps", resp);
        return "limited/stepsHistory";
    }

    @RequestMapping(value = "/user/LocationHistory", method = RequestMethod.GET)
    public String getLocationHistory(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        Iterator<LocationHistory> iterator = locationHistoryService.listAllLocationHistory().iterator();
        ArrayList<LocationHistory> resp = new ArrayList<LocationHistory>();
        while(iterator.hasNext()){
            LocationHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
                resp.add(aux);
            }
        }
        model.addAttribute("user",user);
        model.addAttribute("locations", resp);
        return "limited/locationHistory";
    }

    @RequestMapping(value = "/user/Map", method = RequestMethod.GET)
    public String getMap(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        Iterator<LocationHistory> iterator = locationHistoryService.listAllLocationHistory().iterator();
        int cantidad = 0;
        ArrayList<Float> listLatitud = new ArrayList<>();
        ArrayList<Float> listLongitud = new ArrayList<>();
        ArrayList<String> listName = new ArrayList<>();
        while(iterator.hasNext()){
            LocationHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
                cantidad++;
                listLatitud.add(aux.getLatitude());
                listLongitud.add(aux.getLongitude());
                listName.add("Location " + cantidad);
            }
        }
        model.addAttribute("latitudes", listLatitud);
        model.addAttribute("longitudes", listLongitud);
        model.addAttribute("titulos", listName);
        return "limited/map";
    }

    @RequestMapping(value = "/user/LastLocation", method = RequestMethod.GET)
    public String getLastLocation(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        Iterator<LocationHistory> iterator = locationHistoryService.listAllLocationHistory().iterator();
        int cantidad = 0;
        ArrayList<Float> listLatitud = new ArrayList<>();
        ArrayList<Float> listLongitud = new ArrayList<>();
        ArrayList<String> listName = new ArrayList<>();
        while(iterator.hasNext()){
            LocationHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
                cantidad++;
                listLatitud.add(aux.getLatitude());
                listLongitud.add(aux.getLongitude());
                listName.add("Location " + cantidad);
            }
        }

        if (!listLatitud.isEmpty()) {
            model.addAttribute("latitud", listLatitud.get(listLatitud.size() - 1));
            model.addAttribute("longitud", listLongitud.get(listLongitud.size() - 1));
            model.addAttribute("titulo", listName.get(listName.size() - 1));
        }

        return "limited/mapLastLocation";
    }

    @RequestMapping(value = "/user/distance", method = RequestMethod.GET)
    public String getDistance(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        Iterator<LocationHistory> iterator = locationHistoryService.listAllLocationHistory().iterator();
        ArrayList<LocationHistory> listaUsuario = new ArrayList<>();
        while(iterator.hasNext()){
            LocationHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
                listaUsuario.add(aux);
            }
        }

        Collections.sort(listaUsuario,(l1,l2) -> l1.getDate().compareTo(l2.getDate()));

        // x -> longitud
        // y -> latitud

        float resp = 0;
        ArrayList<String> dialogos = new ArrayList<>();
        for(int index = 0;index < listaUsuario.size() - 1; index++) {
            float valorActual = (float)Math.sqrt(Math.pow(listaUsuario.get(index).getLongitude() - listaUsuario.get(index+1).getLongitude(),2)
                    + Math.pow(listaUsuario.get(index).getLatitude() - listaUsuario.get(index+1).getLongitude(),2));
            dialogos.add("Distancia entre (" + listaUsuario.get(index).getLongitude() + "," + listaUsuario.get(index).getLatitude() +
                ") y (" + listaUsuario.get(index+1).getLongitude() + "," + listaUsuario.get(index+1).getLatitude() + ")    |   Fecha Entre: " +
                    listaUsuario.get(index).getDate() + " y " + listaUsuario.get(index+1).getDate() + "      = " + valorActual
            );
            resp += valorActual;
        }

        model.addAttribute("distanciaTotal", resp);
        model.addAttribute("dialogos",dialogos);
        return "limited/distance";
    }
}